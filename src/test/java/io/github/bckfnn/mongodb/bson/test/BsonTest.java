package io.github.bckfnn.mongodb.bson.test;

import org.junit.Test;

import io.github.bckfnn.mongodb.bson.BsonArray;
import io.github.bckfnn.mongodb.bson.BsonBinary;
import io.github.bckfnn.mongodb.bson.BsonBoolean;
import io.github.bckfnn.mongodb.bson.BsonDatetime;
import io.github.bckfnn.mongodb.bson.BsonDocMap;
import io.github.bckfnn.mongodb.bson.BsonDouble;
import io.github.bckfnn.mongodb.bson.BsonInt;
import io.github.bckfnn.mongodb.bson.BsonLong;
import io.github.bckfnn.mongodb.bson.BsonMaxkey;
import io.github.bckfnn.mongodb.bson.BsonMinkey;
import io.github.bckfnn.mongodb.bson.BsonNull;
import io.github.bckfnn.mongodb.bson.BsonObjectId;
import io.github.bckfnn.mongodb.bson.BsonRegex;
import io.github.bckfnn.mongodb.bson.BsonString;
import io.github.bckfnn.mongodb.bson.BsonTimestamp;
import io.github.bckfnn.mongodb.bson.Element;


public class BsonTest {

    @Test
    public void testHashEquals() {
        Element[] elems = {
                new BsonInt(1),
                new BsonLong(1L),
                new BsonArray(new BsonInt(1), new BsonString("abc")),
                new BsonBinary((byte) 0, new byte[] { 0, 1, 2}),
                new BsonBoolean(true),
                new BsonDatetime(1000l),
                new BsonDocMap(),
                new BsonDouble(1d),
                new BsonMinkey(),
                new BsonMaxkey(),
                new BsonNull(),
                new BsonObjectId(0, 1l),
                new BsonRegex(".*", ""),
                new BsonString("abc"),
                new BsonTimestamp(0, 1),
        };
        
        for (Element e : elems) {
            e.toString();
            e.hashCode();
            e.equals(null);
            e.equals(new Object());
            for (Element o : elems) {
                e.equals(o);
            }
        }
    }
}
 