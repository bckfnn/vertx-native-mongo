package com.github.bckfnn.mongodb.test;

import java.util.List;

import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

import com.github.bckfnn.mongodb.Client;
import com.github.bckfnn.mongodb.Database;
import com.github.bckfnn.mongodb.bson.BsonDoc;


public class DatabaseTest extends TestVerticle {

    @Test
    public void testDropDatabase() throws InterruptedException {
        final Utils.Seq<Client> seq = new Utils.Seq<>();
        seq.add(Utils.open(getVertx(), seq));
        seq.add(new Handler<Client>() {
            public void handle(Client client) {
                Database db = client.database("doesnotexists");
                
                db.dropDatabase(new Utils.Success<BsonDoc>(seq) {
                    @Override
                    public void handle(BsonDoc result) {
                        //System.out.println(result);
                    }
                });
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
    public void testDropDatabase2() throws InterruptedException {
        final Utils.Seq<Database> seq = new Utils.Seq<>();
        seq.add(Utils.open(getVertx(), seq, "testdatabase1"));
        seq.add(new Handler<Database>() {
            public void handle(Database database) {
                database.dropDatabase(new Utils.Success<BsonDoc>(seq) {
                    public void handle(BsonDoc result) {
                        //System.out.println(result);
                    }
                });
            }
        });
        seq.add(new Handler<Database>() {
            public void handle(Database database) {
                database.createCollection("testcollection", new Utils.Success<BsonDoc>(seq) {
                    public void handle(BsonDoc result) {
                        //System.out.println(result);
                    }
                });
            }
        });
        seq.add(new Handler<Database>() {
            public void handle(Database database) {
                database.getClient().getDatabaseNames(new Utils.ToList<String>(new Utils.Success<List<String>>(seq) {
                    public void handle(List<String> result) {
                        VertxAssert.assertTrue(result.contains("testdatabase1"));
                    }
                }));
            }
        });
        seq.add(new Handler<Database>() {
            public void handle(Database database) {
                database.dropDatabase(new Utils.Success<BsonDoc>(seq) {
                    public void handle(BsonDoc result) {
                        //System.out.println(result);
                    }
                });
            }
        });

        seq.add(new Handler<Database>() {
            public void handle(Database database) {
                database.getClient().getDatabaseNames(new Utils.ToList<String>(new Utils.Success<List<String>>(seq) {
                    public void handle(List<String> result) {
                        VertxAssert.assertFalse(result.contains("testdatabase1"));
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
}
