package io.github.bckfnn.mongodb.bson.test;

import org.junit.Assert;
import org.junit.Test;

import io.github.bckfnn.mongodb.bson.BsonBuilder;
import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.bson.JsonEncoder;
import io.vertx.core.buffer.Buffer;


public class BsonBuilderTest {
    @Test
    public void jsonEncoder() {
        BsonDoc doc = BsonBuilder.doc().
                put("hello", "world").get();
        System.out.println(doc);
        Buffer b = JsonEncoder.encode(doc);
        Assert.assertEquals(b.toString(), "{\"hello\":\"world\"}");
    }


    @Test
    public void docBooleans() {
        BsonDoc doc = BsonBuilder.doc().
                put("true", true).
                put("false", false).
                get();
        Assert.assertTrue(doc.getBoolean("true"));
        Assert.assertFalse(doc.getBoolean("false"));
    }

    @Test
    public void docIntegers() {
        BsonDoc doc = BsonBuilder.doc().
                put("zero", 0).
                put("one", 1).
                put("max", Integer.MAX_VALUE).
                put("min", Integer.MIN_VALUE).
                get();
        Assert.assertEquals(0, doc.getInt("zero"));
        Assert.assertEquals(1, doc.getInt("one"));
        Assert.assertEquals(Integer.MAX_VALUE, doc.getInt("max"));
        Assert.assertEquals(Integer.MIN_VALUE, doc.getInt("min"));
    }

    @Test
    public void docLongs() {
        BsonDoc doc = BsonBuilder.doc().
                put("zero", 0L).
                put("one", 1L).
                put("max", Long.MAX_VALUE).
                put("min", Long.MIN_VALUE).
                get();
        Assert.assertEquals(0, doc.getLong("zero"));
        Assert.assertEquals(1, doc.getLong("one"));
        Assert.assertEquals(Long.MAX_VALUE, doc.getLong("max"));
        Assert.assertEquals(Long.MIN_VALUE, doc.getLong("min"));
    }

    @Test
    public void docDoubles() {
        BsonDoc doc = BsonBuilder.doc().
                put("zero", 0d).
                put("one", 1d).
                put("max", Double.MAX_VALUE).
                put("min", Double.MIN_VALUE).
                get();
        Assert.assertEquals(0, doc.getDouble("zero"), 0);
        Assert.assertEquals(1, doc.getDouble("one"), 0);
        Assert.assertEquals(Double.MAX_VALUE, doc.getDouble("max"), 0);
        Assert.assertEquals(Double.MIN_VALUE, doc.getDouble("min"), 0);
    }

    @Test
    public void docBinary() {
        BsonDoc doc = BsonBuilder.doc().
                put("empty", new byte[0]).
                put("one", new byte[] { 1 }).
                get();
        Assert.assertArrayEquals(new byte[0], doc.getBinary("empty"));
        Assert.assertArrayEquals(new byte[] { 1 }, doc.getBinary("one"));
    }

    @Test
    public void subDoc() {
        BsonDoc doc = BsonBuilder.doc().
                putDoc("_").
                put("a", "b").
                get();
        Assert.assertEquals(doc.getDocument("_").getString("a"), "b");
    }

    @Test
    public void subArray() {
        BsonDoc doc = BsonBuilder.doc().
                putArray("_").
                append("a").
                append("b").
                get();
        Assert.assertEquals(doc.getArray("_").getString(0), "a");
        Assert.assertEquals(doc.getArray("_").getString(1), "b");
    }

    @Test
    public void subArrayArray() {
        BsonDoc doc = BsonBuilder.doc().
                putArray("_").
                appendDoc().put("a", "b").pop().
                appendArray().append(1).pop().
                get();
        System.out.println(doc.toString());
        Assert.assertEquals(doc.getArray("_").getDocument(0).getString("a"), "b");
        Assert.assertEquals(doc.getArray("_").getArray(1).getInt(0), 1);
    }

    @Test
    public void subArrayTypes() {
        BsonDoc doc = BsonBuilder.doc().
                putArray("_").
                append("a").
                append(1).
                append(1l).
                append(1d).
                append(true).
                appendNull().
                appendMinkey().
                appendMaxkey().
                get();
        Assert.assertEquals(doc.getArray("_").getString(0), "a");
        Assert.assertEquals(doc.getArray("_").getInt(1), 1);
    }

}


