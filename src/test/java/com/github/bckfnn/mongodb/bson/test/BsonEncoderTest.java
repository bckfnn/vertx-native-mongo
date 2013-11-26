package com.github.bckfnn.mongodb.bson.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.bson.BasicBSONEncoder;
import org.junit.Test;

import com.github.bckfnn.mongodb.bson.BsonArray;
import com.github.bckfnn.mongodb.bson.BsonDoc;
import com.github.bckfnn.mongodb.bson.BsonDocMap;
import com.github.bckfnn.mongodb.bson.BsonDouble;
import com.github.bckfnn.mongodb.bson.BsonEncoder;
import com.github.bckfnn.mongodb.bson.BsonInt;
import com.mongodb.BasicDBObject;


public class BsonEncoderTest {

    @Test
    public void halloWorld() {
        BsonDoc doc = new BsonDocMap();
        doc.putString("hello", "world");

        test(doc,
                "%16%00%00%00%02hello%00%06%00%00%00world%00%00");
    }

    @Test
    public void allTypes() {
        BsonDoc doc = new BsonDocMap();
        doc.putString("string", "String value");
        doc.putInt("int", 42);
        doc.putLong("long", 42424242L);
        doc.putDouble("double", 42.42);
        doc.putBoolean("booleanTrue", true);
        doc.putBoolean("booleanFalse", false);

        BsonDoc sub = new BsonDocMap();
        sub.putString("sub", "sub value");
        doc.putDocument("subdoc", sub);

        BsonArray arr = new BsonArray();
        arr.add(new BsonInt(43));
        arr.add(new BsonDouble(43));
        doc.putArray("array", arr);

        BasicDBObject doc1 = new BasicDBObject();
        doc1.put("string", "String value");
        doc1.put("int", 42);
        doc1.put("long", 42424242L);
        doc1.put("double", 42.42);
        doc1.put("booleanTrue", true);
        doc1.put("booleanFalse", false);

        BasicDBObject sub1 = new BasicDBObject();
        sub1.put("sub", "sub value");
        doc1.put("subdoc", sub1);

        List<Object> arr1 = new ArrayList<Object>();
        arr1.add(43);
        arr1.add(43d);
        doc1.put("array", arr1);

        System.out.println(doc1.toString());

        test(doc, hex(new BasicBSONEncoder().encode(doc1)));

    }

    private void test(BsonDoc doc, String expected) {
        BsonEncoder enc = new BsonEncoder();
        enc.visitDocument(doc);

        Assert.assertEquals(expected, hex(enc.getBuffer().getBytes()));
    }

    String hex(byte[] data) {
        StringBuilder sb = new StringBuilder();

        for (byte b : data) {
            if (b >= ' ' && b <= 127) {
                sb.append((char) b);
            } else {
                String s = Integer.toHexString(b & 0xFF);
                sb.append("%" + (s.length() == 1 ? "0" : "") + s);
            }
        }
        return sb.toString();
    }
}
