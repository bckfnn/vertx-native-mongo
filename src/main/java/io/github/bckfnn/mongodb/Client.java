/*
 * Copyright 2016 Finn Bock
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bckfnn.mongodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.bckfnn.callback.Callback;
import io.github.bckfnn.mongodb.bson.BsonDecoder;
import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.bson.BsonEncoder;
import io.github.bckfnn.mongodb.bson.Element;
import io.github.bckfnn.mongodb.msg.KillCursors;
import io.github.bckfnn.mongodb.msg.OutMessage;
import io.github.bckfnn.mongodb.msg.Reply;
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

    private Map<Integer, Callback<Reply>> callbacks = new HashMap<>();

    private int maxBsonSize = 16 * 1024 * 1024;
    int outstanding = 0;

	public Client(Vertx vertx, String host, int port) {
	    this.vertx = vertx;
		this.host = host;
		this.port = port;
	}

	public void open(final Callback<Client> callback) {

        netClient = vertx.createNetClient();

        netClient.connect(port, host, result -> {
            if (result.failed()) {
                callback.fail(result.cause());
            } else {
                netSocket = result.result();

                final RecordParser rp = RecordParser.newFixed(4, null);
                rp.setOutput(new Handler<Buffer>() {
                    boolean s = true;

                    @Override
                    public void handle(Buffer buffer) {
                        //System.out.println("handle:" + buffer.length());
                        if (s) {
                            int size = Integer.reverseBytes(buffer.getInt(0));
                            //System.out.println(size);
                            rp.fixedSizeMode(size - 4);
                        } else {
                            Reply reply = new Reply();
                            reply.read(new BsonDecoder(buffer));
                            //System.out.println(reply);
                            System.out.println(reply.getResponseId() + " " + reply.getDocuments());
                            Callback<Reply> callback = callbacks.remove(reply.getResponseId());
                            if (callback != null) {
                                outstanding--;
                                callback.ok(reply);
                            } else {
                                System.err.println("no callback:" + callback + "  for " + reply.getResponseId());
                            }
                            rp.fixedSizeMode(4);
                        }
                        s = !s;
                    }

                });
                netSocket.closeHandler($ -> {
                    System.err.println("close handler");
                });
                netSocket.exceptionHandler(e -> {
                    System.err.println("exception " + e);
                });
                netSocket.handler(rp);

                database("admin").command("ismaster", 0, 1, callback.call(r -> {
                    maxBsonSize = r.getInt("maxBsonObjectSize");

                    System.out.println("got ismaster callback");
                    callback.ok(this);
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

    public void write(OutMessage msg, OutMessage lastError, final Callback<Reply> callback) {
        msg.setRequestId(++requestId);
        System.out.println("write-request:" + requestId + " " + msg);

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

    public void getDatabaseNames(final Callback<List<String>> callback) {
        listDatabases(callback.call(result -> {
            List<String> list = new ArrayList<>();
            for (BsonDoc doc : result) {
                list.add(doc.getString("name"));
            }
            callback.ok(list);
        }));
    }

    public void listDatabases(final Callback<List<BsonDoc>> callback) {
        database("admin").command("listDatabases", 0, -1, callback.call(result -> {
            List<BsonDoc> list = new ArrayList<>();
            for (Element e : result.getArray("databases")) {
                list.add((BsonDoc) e);
            }
            callback.ok(list);
        }));
    }


    public void fsync(boolean async, Callback<BsonDoc> callback) {
        BsonDoc cmd = BsonDoc.newDoc(n -> {
            n.put("fsync", 1);
            if (async) {
                n.put("async", 1);
            }
        });
        database("admin").command(cmd, 0, 1, callback);
    }

    public void fsyncAndLock(Callback<BsonDoc> callback) {
        BsonDoc cmd = BsonDoc.newDoc(d -> {
            d.put("fsync", 1);
            d.put("lock", 1);
        });

        database("admin").command(cmd, 0, 1, callback);
    }

    public void isLocked(Callback<Boolean> callback) {
        Database admin = database("admin");
        Collection col = admin.collection("$cmd.sys.inprog");
        col.__find(null, null, 0, 1, callback.one(result -> {
            if (result.get("fsyncLock") != null) {
                callback.ok(result.getInt("fsyncLock") == 1);
            } else {
                callback.ok(false);
            }
        }));
    }

    /**
     * Unlocks the database, allowing the write operations to go through.
     * This command may be asynchronous on the server, which means there may be a small delay before the database becomes writable.
     * @param callback
     */
    public void unlock(Callback<BsonDoc> callback) {
        Database admin = database("admin");
        Collection col = admin.collection("$cmd.sys.unlock");
        col.__find(null, null, 0, 1, callback.one(result -> {
            callback.ok(result);;
        }));
    }


    public void killCursor(List<Long> cursors, final Callback<Boolean> callback) {
        KillCursors cmd = new KillCursors();
        cmd.setCursorIds(cursors);
        write(cmd, null, callback.call(result -> {
            callback.ok(true);
        }));
    }

    public void serverInfo(Callback<BsonDoc> callback) {
        database("admin").command("buildinfo", 0, 1, callback);
    }

    public void dropDatabase(String name, Callback<BsonDoc> callback) {
        database(name).dropDatabase(callback);
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
