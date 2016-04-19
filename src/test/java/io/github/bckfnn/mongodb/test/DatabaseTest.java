package io.github.bckfnn.mongodb.test;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.bckfnn.mongodb.Client;
import io.github.bckfnn.mongodb.Database;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class DatabaseTest {
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void testDropDatabase(TestContext context) throws InterruptedException {
        final Client client = new Client(rule.vertx(), "localhost", 27017);
        client.open(context.asyncAssertSuccess($ -> {
            Database db = client.database("doesnotexists");

            db.dropDatabase(context.asyncAssertSuccess(result -> {
                System.out.println(result);
            }));
        }));
    }

    @Test
    public void testDropDatabase2(TestContext context) throws InterruptedException {
        final Client client = new Client(rule.vertx(), "localhost", 27017);
        client.open(context.asyncAssertSuccess($ -> {
            Database db = client.database("testdatabase1");

            db.dropDatabase(context.asyncAssertSuccess(result -> {
                db.createCollection("testcollection", context.asyncAssertSuccess(result2 -> {
                    db.getClient().getDatabaseNames(context.asyncAssertSuccess(list -> {
                        context.assertTrue(list.contains("testdatabase1"));
                        db.dropDatabase(context.asyncAssertSuccess(result3 -> {
                            db.getClient().getDatabaseNames(context.asyncAssertSuccess(list2 -> {
                                context.assertFalse(list2.contains("testdatabase1"));
                            }));
                        }));
                    }));
                }));
            }));
        }));
    }
}
