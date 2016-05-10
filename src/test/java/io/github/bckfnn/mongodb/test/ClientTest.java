package io.github.bckfnn.mongodb.test;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.bckfnn.callback.Callback;
import io.github.bckfnn.mongodb.Client;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)

public class ClientTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();
    
    
    @Test
    public void testServerInfo(TestContext context) {
        final Client client = new Client(rule.vertx(), "localhost", 27017);
        client.open(Callback.callback(context.asyncAssertSuccess($ -> {
            client.serverInfo(Callback.callback(context.asyncAssertSuccess(doc -> {
                //System.out.println(doc);
                context.assertEquals("3.2.4", doc.getString("version"));
                context.assertEquals("tcmalloc", doc.getString("allocator"));
                context.assertEquals(64, doc.getNumber("bits"));
                context.assertEquals(client.getMaxBsonSize(), doc.getNumber("maxBsonObjectSize"));
            })));
        })));
    }
    
    @Test
    public void testFsync(TestContext context) {
        final Client client = new Client(rule.vertx(), "localhost", 27017);
        client.open(Callback.callback(context.asyncAssertSuccess($ -> {
            client.fsync(false, Callback.callback(context.asyncAssertSuccess(doc -> {
                context.assertEquals(1, doc.getNumber("numFiles"));
                client.fsync(true,Callback.callback( context.asyncAssertSuccess(doc2 -> {
                    context.assertEquals(1, doc.getNumber("numFiles"));
                })));
            })));
        })));
    }
}
