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

import org.junit.Test;

import io.github.bckfnn.mongodb.bson.BsonArrayList;
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
                new BsonArrayList(new BsonInt(1), new BsonString("abc")),
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
