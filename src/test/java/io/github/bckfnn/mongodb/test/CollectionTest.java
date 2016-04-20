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
package io.github.bckfnn.mongodb.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.bckfnn.mongodb.Client;
import io.github.bckfnn.mongodb.Collection;
import io.github.bckfnn.mongodb.Database;
import io.github.bckfnn.mongodb.Utils;
import io.github.bckfnn.mongodb.WriteConcern;
import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;


@RunWith(VertxUnitRunner.class)
public class CollectionTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void testDatabases(TestContext context) throws InterruptedException {
        final Client client = new Client(rule.vertx(), "localhost", 27017);
        client.open(context.asyncAssertSuccess($ -> {
            client.getDatabaseNames(context.asyncAssertSuccess(list -> {
                Collections.sort(list);

                Collection collection = client.database("test_X").collection("coll");

                List<BsonDoc> data = new ArrayList<>();
                BsonDoc val = BsonDoc.newDoc(d -> d.put("a", "value"));
                data.add(val);

                collection.insert(data, WriteConcern.SAFE, context.asyncAssertSuccess(result -> {
                    context.assertEquals(1, (int) result.getOk());

                    client.getDatabaseNames(context.asyncAssertSuccess(list2 -> {
                        context.assertTrue(list2.contains("test_X"));

                        client.dropDatabase("test_X", context.asyncAssertSuccess(r -> {
                            context.assertEquals(1.0, r.getDouble("ok"));

                            client.getDatabaseNames(context.asyncAssertSuccess(list3 -> {
                                context.assertFalse(list3.contains("test_X"));
                            }));
                        }));

                    }));
                }));


            }));
        }));
    }


    @Test
    public void testCollections(TestContext context) throws InterruptedException {

        final Client client = new Client(rule.vertx(), "localhost", 27017);
        client.open(context.asyncAssertSuccess($ -> {
            Database database =  client.database("test_Y");

            database.dropDatabase(context.asyncAssertSuccess(doc -> {
                context.assertEquals(1.0, doc.getDouble("ok"));

                database.collectionNames(rs -> {
                    Utils.collect(rs, context.asyncAssertSuccess(result -> {
                        context.assertEquals(0, result.size());

                        Collection collection = database.collection("coll");

                        List<BsonDoc> data = new ArrayList<>();
                        BsonDoc val = BsonDoc.newDoc(d -> d.put("a", "value"));
                        data.add(val);

                        collection.insert(data, WriteConcern.SAFE, context.asyncAssertSuccess(writeResult -> {
                            context.assertEquals(1, (int) writeResult.getOk());

                            database.collectionNames(rs2 -> {
                                Utils.collect(rs, context.asyncAssertSuccess(result2 -> {
                                    context.assertEquals(1, result2.size());
                                    context.assertEquals(Arrays.asList("coll"), result2);
                                }));
                            });
                        }));
                    }));
                });
            }));

        }));
    }

    @Test
    public void testInsert(TestContext context) throws InterruptedException {
        final Client client = new Client(rule.vertx(), "localhost", 27017);
        client.open(context.asyncAssertSuccess($ -> {
            Database database =  client.database("test_Z");
            database.dropDatabase(context.asyncAssertSuccess(doc -> {
                context.assertEquals(1.0, doc.getDouble("ok"));

                Collection collection = database.collection("coll");

                BsonDoc val = BsonDoc.newDoc(d -> d.put("a", "value"));

                collection.insert(Arrays.asList(val), WriteConcern.SAFE, context.asyncAssertSuccess(writeResult -> {
                    context.assertEquals(1, (int) writeResult.getOk());

                    database.collection("coll").count(context.asyncAssertSuccess(count1 -> {
                        context.assertEquals(1l, count1.longValue());

                        BsonDoc val2 = BsonDoc.newDoc(d -> d.put("a", "value"));

                        collection.insert(Arrays.asList(val2), WriteConcern.SAFE, context.asyncAssertSuccess(writeResult2 -> {
                            context.assertEquals(1, (int) writeResult2.getOk());

                            database.collection("coll").count(context.asyncAssertSuccess(count2 -> {
                                context.assertEquals(2l, count2.longValue());
                            }));
                        }));
                    }));
                }));
            }));
        }));
    }

}
