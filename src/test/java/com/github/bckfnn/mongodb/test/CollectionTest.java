package com.github.bckfnn.mongodb.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

import com.github.bckfnn.mongodb.Client;
import com.github.bckfnn.mongodb.Collection;
import com.github.bckfnn.mongodb.Database;
import com.github.bckfnn.mongodb.WriteConcern;
import com.github.bckfnn.mongodb.WriteResult;
import com.github.bckfnn.mongodb.bson.BsonBuilder;
import com.github.bckfnn.mongodb.bson.BsonDoc;


public class CollectionTest extends TestVerticle {
    @Test
    public void testDatabases() throws InterruptedException {
        final Utils.Seq<Client> seq = new Utils.Seq<>();
        seq.add(Utils.open(getVertx(), seq));
        seq.add(new Handler<Client>() {
            @Override
            public void handle(Client client) {
                client.getDatabaseNames(new Utils.ToList<String>(new Utils.Success<List<String>>(seq) {
                    @Override
                    public void handle(List<String> result) {
                        Collections.sort(result);
                        //System.out.println(result);
                    }
                }));
            }
        });
        seq.add(new Handler<Client>() {
            public void handle(Client client) {
                Collection collection = client.database("test_X").collection("coll");

                List<BsonDoc> data = new ArrayList<>();
                BsonDoc val = BsonBuilder.doc().put("a", "value").get();
                data.add(val);

                collection.insert(data, WriteConcern.SAFE, new Utils.Success<WriteResult>(seq) {
                    @Override
                    public void handle(WriteResult result) {
                        VertxAssert.assertEquals(1, (int) result.getOk());
                    }
                });
            };
        });
        seq.add(new Handler<Client>() {
            @Override
            public void handle(Client client) {
                client.getDatabaseNames(new Utils.ToList<String>(new Utils.Success<List<String>>(seq) {
                    @Override
                    public void handle(List<String> result) {
                        VertxAssert.assertTrue(result.contains("test_X"));
                    }
                }));
            }
        });
        seq.add(new Handler<Client>() {
            @Override
            public void handle(Client client) {
                client.dropDatabase("test_X", new Utils.Success<BsonDoc>(seq) {
                    @Override
                    public void handle(BsonDoc result) {
                        VertxAssert.assertEquals(1.0, result.getDouble("ok"), 0);
                    }
                });
            }
        });
        seq.add(new Handler<Client>() {
            @Override
            public void handle(Client client) {
                client.getDatabaseNames(new Utils.ToList<String>(new Utils.Success<List<String>>(seq) {
                    @Override
                    public void handle(List<String> result) {
                        VertxAssert.assertFalse(result.contains("test_X"));
                    }
                }));
            }
        });
        seq.add(new Handler<Client>() {
            @Override
            public void handle(Client client) {
                VertxAssert.testComplete();
            }
        });
        seq.next();
    }

    @Test
    public void testCollections() throws InterruptedException {
        final Utils.Seq<Database> seq = new Utils.Seq<>();
        seq.add(Utils.open(getVertx(), seq, "test_Y"));
        seq.add(new Handler<Database>() {
            @Override
            public void handle(Database database) {
                database.dropDatabase(new Utils.Success<BsonDoc>(seq) {
                    @Override
                    public void handle(BsonDoc result) {
                        VertxAssert.assertEquals(1.0, result.getDouble("ok"), 0);
                    }
                });
            }
        });
        seq.add(new Handler<Database>() {
            @Override
            public void handle(Database database) {
                database.collectionNames(new Utils.ToList<String>(new Utils.Success<List<String>>(seq) {
                    @Override
                    public void handle(List<String> result) {
                        VertxAssert.assertEquals(0, result.size());
                    }
                }));
            }
        });
        seq.add(new Handler<Database>() {
            public void handle(Database database) {
                Collection collection = database.collection("coll");

                List<BsonDoc> data = new ArrayList<>();
                BsonDoc val = BsonBuilder.doc().put("a", "value").get();
                data.add(val);

                collection.insert(data, WriteConcern.SAFE, new Utils.Success<WriteResult>(seq) {
                    @Override
                    public void handle(WriteResult result) {
                        VertxAssert.assertEquals(1, (int) result.getOk());
                    }
                });
            };
        });
        seq.add(new Handler<Database>() {
            @Override
            public void handle(Database database) {
                database.collectionNames(new Utils.ToList<String>(new Utils.Success<List<String>>(seq) {
                    @Override
                    public void handle(List<String> result) {
                        VertxAssert.assertEquals(1, result.size());
                        VertxAssert.assertEquals(Arrays.asList("coll"), result);
                    }
                }));
            }
        });
        seq.add(new Handler<Database>() {
            @Override
            public void handle(Database database) {
                VertxAssert.testComplete();
            }
        });
        seq.next();
    }

    @Test
    public void testInsert() throws InterruptedException {
        final Utils.Seq<Database> seq = new Utils.Seq<>();
        seq.add(Utils.open(getVertx(), seq, "test_Z"));
        seq.add(new Handler<Database>() {
            @Override
            public void handle(Database database) {
                database.dropDatabase(new Utils.Success<BsonDoc>(seq) {
                    @Override
                    public void handle(BsonDoc result) {
                        VertxAssert.assertEquals(1.0, result.getDouble("ok"), 0);
                    }
                });
            }
        });
        seq.add(new Handler<Database>() {
            public void handle(Database database) {
                Collection collection = database.collection("coll");

                List<BsonDoc> data = new ArrayList<>();
                BsonDoc val = BsonBuilder.doc().put("a", "value").get();
                data.add(val);

                collection.insert(data, WriteConcern.SAFE, new Utils.Success<WriteResult>(seq) {
                    @Override
                    public void handle(WriteResult result) {
                        VertxAssert.assertEquals(1, (int) result.getOk());
                    }
                });
            };
        });
        seq.add(new Handler<Database>() {
            @Override
            public void handle(Database database) {
                database.collection("coll").count(new Utils.Success<Long>(seq) {
                    @Override
                    public void handle(Long result) {
                        VertxAssert.assertEquals(1l, result.longValue());
                    }
                });
            }
        });
        
        seq.add(new Handler<Database>() {
            public void handle(Database database) {
                Collection collection = database.collection("coll");

                List<BsonDoc> data = new ArrayList<>();
                BsonDoc val = BsonBuilder.doc().put("a", "value").get();
                data.add(val);

                collection.insert(data, WriteConcern.SAFE, new Utils.Success<WriteResult>(seq) {
                    @Override
                    public void handle(WriteResult result) {
                        VertxAssert.assertEquals(1, (int) result.getOk());
                    }
                });
            };
        });
        seq.add(new Handler<Database>() {
            @Override
            public void handle(Database database) {
                database.collection("coll").count(new Utils.Success<Long>(seq) {
                    @Override
                    public void handle(Long result) {
                        VertxAssert.assertEquals(2l, result.longValue());
                    }
                });
            }
        });
        seq.add(new Handler<Database>() {
            @Override
            public void handle(Database database) {
                VertxAssert.testComplete();
            }
        });
        seq.next();
    }





    void open(final Handler<Client> handler) {
        final Client client = new Client(getVertx(), "localhost", 27017);
        client.open(new Handler<AsyncResult<Client>>() {
            @Override
            public void handle(AsyncResult<Client> event) {
                VertxAssert.assertTrue(event.succeeded());
                handler.handle(client);
            }
        });
    }

 
 
 
}
