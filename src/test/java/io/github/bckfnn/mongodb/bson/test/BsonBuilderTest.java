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
package io.github.bckfnn.mongodb.bson.test;

import org.junit.Assert;
import org.junit.Test;

import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.bson.JsonEncoder;
import io.vertx.core.buffer.Buffer;


public class BsonBuilderTest {
    @Test
    public void jsonEncoder() {
        BsonDoc doc = BsonDoc.newDoc(d -> {
            d.put("hello", "world");
        });

        Buffer b = JsonEncoder.encode(doc);
        Assert.assertEquals(b.toString(), "{\"hello\":\"world\"}");
    }


    @Test
    public void docBooleans() {
        BsonDoc doc = BsonDoc.newDoc(d -> {
            d.put("true", true);
            d.put("false", false);
        });

        Assert.assertTrue(doc.getBoolean("true"));
        Assert.assertFalse(doc.getBoolean("false"));
    }

    @Test
    public void docIntegers() {
        BsonDoc doc = BsonDoc.newDoc(d -> {
            d.put("zero", 0);
            d.put("one", 1);
            d.put("max", Integer.MAX_VALUE);
            d.put("min", Integer.MIN_VALUE);
        });
        Assert.assertEquals(0, doc.getInt("zero"));
        Assert.assertEquals(1, doc.getInt("one"));
        Assert.assertEquals(Integer.MAX_VALUE, doc.getInt("max"));
        Assert.assertEquals(Integer.MIN_VALUE, doc.getInt("min"));
    }

    @Test
    public void docLongs() {
        BsonDoc doc = BsonDoc.newDoc(d -> {
            d.put("zero", 0L);
            d.put("one", 1L);
            d.put("max", Long.MAX_VALUE);
            d.put("min", Long.MIN_VALUE);
        });
        Assert.assertEquals(0, doc.getLong("zero"));
        Assert.assertEquals(1, doc.getLong("one"));
        Assert.assertEquals(Long.MAX_VALUE, doc.getLong("max"));
        Assert.assertEquals(Long.MIN_VALUE, doc.getLong("min"));
    }

    @Test
    public void docDoubles() {
        BsonDoc doc = BsonDoc.newDoc(d -> {
            d.put("zero", 0d);
            d.put("one", 1d);
            d.put("max", Double.MAX_VALUE);
            d.put("min", Double.MIN_VALUE);
        });
        Assert.assertEquals(0, doc.getDouble("zero"), 0);
        Assert.assertEquals(1, doc.getDouble("one"), 0);
        Assert.assertEquals(Double.MAX_VALUE, doc.getDouble("max"), 0);
        Assert.assertEquals(Double.MIN_VALUE, doc.getDouble("min"), 0);
    }

    @Test
    public void docBinary() {
        BsonDoc doc = BsonDoc.newDoc(d -> {
            d.put("empty", new byte[0]);
            d.put("one", new byte[] { 1 });
        });
        Assert.assertArrayEquals(new byte[0], doc.getBinary("empty"));
        Assert.assertArrayEquals(new byte[] { 1 }, doc.getBinary("one"));
    }

    @Test
    public void subDoc() {
        BsonDoc doc = BsonDoc.newDoc(d -> {
            d.putDoc("_", d2 -> {
                d2.put("a", "b");
            });
        });
        Assert.assertEquals(doc.getDocument("_").getString("a"), "b");
    }

    @Test
    public void subArray() {
        BsonDoc doc = BsonDoc.newDoc(d -> {
            d.putArray("_", a -> {
                a.add("a");
                a.add("b");
            });
        });
        Assert.assertEquals(doc.getArray("_").getString(0), "a");
        Assert.assertEquals(doc.getArray("_").getString(1), "b");
    }

    @Test
    public void subArrayArray() {
        BsonDoc doc = BsonDoc.newDoc(d -> {
            d.putArray("_", a -> {
                a.addDoc(d2 -> {
                    d2.put("a", "b");
                });
                a.addArray(a2 -> {
                    a2.add(1);
                });
            });
        });

        Assert.assertEquals(doc.getArray("_").getDocument(0).getString("a"), "b");
        Assert.assertEquals(doc.getArray("_").getArray(1).getInt(0), 1);
    }

    @Test
    public void subArrayTypes() {
        BsonDoc doc = BsonDoc.newDoc(d -> {
            d.putArray("_", a -> {
                a.add("a");
                a.add(1);
                a.add(1l);
                a.add(1d);
                a.add(true);
                a.addNull();
                a.addMinkey();
                a.addMaxkey();
            });
        });
        Assert.assertEquals(doc.getArray("_").getString(0), "a");
        Assert.assertEquals(doc.getArray("_").getInt(1), 1);
    }

}


