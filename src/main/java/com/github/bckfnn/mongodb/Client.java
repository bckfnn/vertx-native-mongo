package com.github.bckfnn.mongodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.bckfnn.mongodb.bson.BsonBuilder;
import com.github.bckfnn.mongodb.bson.BsonDecoder;
import com.github.bckfnn.mongodb.bson.BsonDoc;
import com.github.bckfnn.mongodb.bson.BsonEncoder;
import com.github.bckfnn.mongodb.bson.Element;
import com.github.bckfnn.mongodb.msg.KillCursors;
import com.github.bckfnn.mongodb.msg.OutMessage;
import com.github.bckfnn.mongodb.msg.Reply;
import com.mongodb.MongoException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;


public class Client {
	String host;
	int port;

    private Vertx vertx;
    private NetClient netClient;
    private NetSocket netSocket;
    private int requestId;

    private Map<Integer, Handler<AsyncResult<Reply>>> callbacks = new HashMap<>();

    private int maxBsonSize = 16 * 1024 * 1024;
    int outstanding = 0;

	public Client(Vertx vertx, String host, int port) {
	    this.vertx = vertx;
		this.host = host;
		this.port = port;
	}

	public void open(final Handler<AsyncResult<Client>> handler) {

        netClient = vertx.createNetClient();

        netClient.connect(port, host, result -> {
            if (result.failed()) {
                handler.handle(Future.failedFuture(result.cause()));
            } else {
                netSocket = result.result();

                final RecordParser rp = RecordParser.newFixed(4, null);
                rp.setOutput(new Handler<Buffer>() {
                    boolean s = true;

                    @Override
                    public void handle(Buffer buffer) {
                        System.out.println("handle:" + buffer.length());
                        if (s) {
                            int size = Integer.reverseBytes(buffer.getInt(0));
                            //System.out.println(size);
                            rp.fixedSizeMode(size - 4);
                        } else {
                            Reply reply = new Reply();
                            reply.read(new BsonDecoder(buffer));
                            System.out.println(reply);
                            System.out.println(reply.getDocuments());
                            Handler<AsyncResult<Reply>> callback = callbacks.remove(reply.getResponseId());
                            System.out.println("found callback:" + callback + "  for " + reply.getResponseId());
                            if (callback != null) {
                                outstanding--;
                                callback.handle(Future.succeededFuture(reply));
                            }
                            rp.fixedSizeMode(4);
                        }
                        s = !s;
                    }

                });

                netSocket.handler(rp);

                database("admin").command("ismaster", 0, 1, Utils.handler(handler, r -> {
                    maxBsonSize = r.getInt("maxBsonObjectSize");

                    System.out.println("got ismaster callback");
                    handler.handle(Future.succeededFuture(this));
                }));
            }
        });
    }

    public void close() {
        netClient.close();
        netSocket.close();
    }

    public Database database(String name) {
    	return new Database(this, name);
    }

    public void write(OutMessage msg, OutMessage lastError, final Handler<AsyncResult<Reply>> callback) {
        msg.setRequestId(++requestId);
        System.out.println("write-request:" + requestId);

        BsonEncoder enc = new BsonEncoder();
        msg.send(enc);
        if (lastError != null) {
            lastError.setRequestId(++requestId);
            lastError.send(enc);
        }

        if (msg.hasResponse() || lastError != null) {
            outstanding++;
            System.out.println("outstanding:" + outstanding);
            callbacks.put(requestId, callback);
            netSocket.write(enc.getBuffer());
        } else {
            netSocket.write(enc.getBuffer());
        }
    }

    public void getDatabaseNames(final Handler<AsyncResult<List<String>>> handler) {
        listDatabases(Utils.handler(handler, result -> {
            List<String> list = new ArrayList<>();
            for (BsonDoc doc : result) {
                list.add(doc.getString("name"));
            }
            handler.handle(Future.succeededFuture(list));
        }));
    }

    public void listDatabases(final Handler<AsyncResult<List<BsonDoc>>> handler) {
        database("admin").command("listDatabases", 0, -1, Utils.handler(handler, result -> {
            List<BsonDoc> list = new ArrayList<>();
            for (Element e : result.getArray("databases")) {
                list.add((BsonDoc) e);
            }
            handler.handle(Future.succeededFuture(list));
        }));
    }


    public void fsync(boolean async, Handler<AsyncResult<BsonDoc>> handler) {
        BsonDoc cmd = BsonBuilder.doc().put("fsync", 1).get();
        if (async) {
            cmd.putInt("async", 1);
        }
        database("admin").command(cmd, 0, 1, handler);
    }

    public void fsyncAndLock(Handler<AsyncResult<BsonDoc>> handler) {
        BsonDoc cmd = BsonBuilder.doc().put("fsync", 1).put("lock", 1).get();

        database("admin").command(cmd, 0, 1, handler);
    }

    public void isLocked(Handler<AsyncResult<Boolean>> handler) {
        Database admin = database("admin");
        Collection col = admin.collection("$cmd.sys.inprog");
        col.__find(null, null, 0, 1, Utils.one(handler,  result -> {
            if (result.get("fsyncLock") != null) {
                handler.handle(Future.succeededFuture(result.getInt("fsyncLock") == 1));
            } else {
                handler.handle(Future.succeededFuture(false));
            }
        }));
    }

    /**
     * Unlocks the database, allowing the write operations to go through.
     * This command may be asynchronous on the server, which means there may be a small delay before the database becomes writable.
     * @param handler
     * @throws MongoException
     */
    public void unlock(Handler<AsyncResult<BsonDoc>> handler) {
        Database admin = database("admin");
        Collection col = admin.collection("$cmd.sys.unlock");
        col.__find(null, null, 0, 1, Utils.one(handler, result -> {
            handler.handle(Future.succeededFuture(result));
        }));
    }


    public void killCursor(List<Long> cursors, final Handler<AsyncResult<Boolean>> handler) {
        KillCursors cmd = new KillCursors();
        cmd.setCursorIds(cursors);
        write(cmd, null, Utils.handler(handler, result -> {
            handler.handle(Future.succeededFuture(true));
        }));
    }

    public void serverInfo(Handler<AsyncResult<BsonDoc>> callback) {
        database("admin").command("buildinfo", 0, 1, callback);
    }

    public void dropDatabase(String name, final Handler<AsyncResult<BsonDoc>> handler) {
        database(name).dropDatabase(handler);
    }

    /*
     * alive = AsyncRead()

     * close_cursor = AsyncCommand()
     * copy_database()
     * drop_database = AsyncCommand().unwrap('MotorDatabase')
     * fsync
     * kill_cursors = AsyncCommand()
     * server_info = AsyncRead()
     * unlock = AsyncCommand()
     * connected
     * is_primary = ReadOnlyProperty()
     * is_mongos = ReadOnlyProperty()
     * max_pool_size = ReadOnlyProperty()
	 * tz_aware = ReadOnlyProperty()


     * get_default_database = DelegateMethod()
     * nodes = ReadOnlyProperty()
     * host = ReadOnlyProperty()
     * port = ReadOnlyProperty()

     * primary = ReadOnlyProperty()
     * secondaries = ReadOnlyProperty()
     * arbiters = ReadOnlyProperty()
     * hosts = ReadOnlyProperty()
     * seeds = ReadOnlyProperty()
     */

	/**
     * @return the maxBsonSize
     */
    public int getMaxBsonSize() {
        return maxBsonSize;
    }

}