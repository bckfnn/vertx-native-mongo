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
