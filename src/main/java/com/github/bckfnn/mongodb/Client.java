package com.github.bckfnn.mongodb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.core.parsetools.RecordParser;

import com.github.bckfnn.mongodb.bson.BsonBuilder;
import com.github.bckfnn.mongodb.bson.BsonDecoder;
import com.github.bckfnn.mongodb.bson.BsonDoc;
import com.github.bckfnn.mongodb.bson.BsonEncoder;
import com.github.bckfnn.mongodb.bson.Element;
import com.github.bckfnn.mongodb.msg.KillCursors;
import com.github.bckfnn.mongodb.msg.OutMessage;
import com.github.bckfnn.mongodb.msg.Reply;
import com.mongodb.MongoException;


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
	
	public void open(final Handler<AsyncResult<Client>> handler) {
		
        netClient = vertx.createNetClient();
 
        netClient.connect(port, host, new Handler<AsyncResult<NetSocket>>() {
            @Override
            public void handle(AsyncResult<NetSocket> result) {
                if (result.failed()) {
                    handler.handle(new Result<Client>(result.cause()));
                } else {
                    netSocket = result.result();


                    final RecordParser rp = RecordParser.newFixed(4, null);
                    rp.setOutput(new Handler<Buffer>() {
                        boolean s = true;

                        @Override
                        public void handle(Buffer buffer) {
                            if (s) {
                                int size = Integer.reverseBytes(buffer.getInt(0));
                                //System.out.println(size);
                                rp.fixedSizeMode(size - 4);
                            } else {
                                Reply reply = new Reply();
                                reply.read(new BsonDecoder(buffer));
                                //System.out.println(reply);
                                //System.out.println(reply.getDocuments());
                                Callback<Reply> callback = callbacks.remove(reply.getResponseId());
                                if (callback != null) {
                                    outstanding--;
                                    callback.handle(reply);
                                    callback.end();
                                }
                                rp.fixedSizeMode(4);
                            }
                            s = !s;
                        }

                    });
                    
                    netSocket.dataHandler(rp);

                    database("admin").command("ismaster", 0, 1, new Callback<BsonDoc>() {
                        @Override
                        public void handle(BsonDoc result) {
                            maxBsonSize = result.getInt("maxBsonObjectSize");
                        }

                        @Override
                        public void error(Throwable error) {
                            handler.handle(new Result<Client>(error));
                        }

                        @Override
                        public void end() {
                            handler.handle(new Result<>(Client.this));
                        }
                    });
                }
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
        //System.out.println("write-request:" + requestId);

        BsonEncoder enc = new BsonEncoder();
        msg.send(enc);
        if (lastError != null) {
            lastError.setRequestId(++requestId);
            lastError.send(enc);
        }

        if (msg.hasResponse() || lastError != null) {
            outstanding++;
            //System.out.println(outstanding);
            callbacks.put(requestId, callback);
            netSocket.write(enc.getBuffer());
        } else {
            netSocket.write(enc.getBuffer());
            callback.end();
        }
    }
    
    public void getDatabaseNames(final Callback<String> handler) {
        listDatabases(new Callback.Default<BsonDoc>(handler) {
            @Override
            public void handle(BsonDoc result) {
                handler.handle(result.getString("name"));
            }
        });
    }

    public void listDatabases(final Callback<BsonDoc> callback) {
        database("admin").command("listDatabases", 0, -1, new Callback.Default<BsonDoc>(callback) {
            @Override
            public void handle(BsonDoc result) {
                for (Element e : result.getArray("databases")) {
                    callback.handle((BsonDoc) e);
                }
            }
        });
    }
    
    
    public void fsync(boolean async, Callback<BsonDoc> callback) {
        BsonDoc cmd = BsonBuilder.doc().put("fsync", 1).get();
        if (async) {
            cmd.putInt("async", 1); 
        }
        database("admin").command(cmd, 0, 1, callback);
    }
    
    public void fsyncAndLock(Callback<BsonDoc> callback) {
        BsonDoc cmd = BsonBuilder.doc().put("fsync", 1).put("lock", 1).get();
 
        database("admin").command(cmd, 0, 1, callback);
    }
    
    public void isLocked(final Callback<Boolean> callback) {
        Database admin = database("admin");
        Collection col = admin.collection("$cmd.sys.inprog");
        col.__find(null, null, 0, 1, new Callback.Default<BsonDoc>(callback) {
            @Override
            public void handle(BsonDoc result) {
                if (result.get("fsyncLock") != null) {
                    callback.handle(result.getInt("fsyncLock") == 1);
                }
            }
        });
 
    }
    
    /**
     * Unlocks the database, allowing the write operations to go through.
     * This command may be asynchronous on the server, which means there may be a small delay before the database becomes writable.
     * @return
     * @throws MongoException
     */
    public void unlock(Callback<BsonDoc> callback) {
        Database admin = database("admin");
        Collection col = admin.collection("$cmd.sys.unlock");
        col.__find(null, null, 0, 1, callback);
    }
    

    public void killCursor(List<Long> cursors, final Callback<Boolean> callback) {
        KillCursors cmd = new KillCursors();
        cmd.setCursorIds(cursors);
        write(cmd, null, new Callback.Default<Reply>(callback) {
            @Override
            public void handle(Reply result) {
                callback.handle(true);
            }
        });
    }

    public void serverInfo(final Callback<BsonDoc> callback) {
        database("admin").command("buildinfo", 0, 1, callback);
    }
    
    public void dropDatabase(String name, final Callback<BsonDoc> callback) {
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





    static class Result<T> implements AsyncResult<T> {
		Throwable error;
		T result;
		
		public Result(Throwable error) {
			this.error = error;
		}

		public Result(T result) {
			this.result = result;
		}

		@Override
		public T result() {
			return result;
		}

		@Override
		public Throwable cause() {
			return error;
		}

		@Override
		public boolean succeeded() {
			return error == null;
		}

		@Override
		public boolean failed() {
			return error != null;
		}
	}
}
