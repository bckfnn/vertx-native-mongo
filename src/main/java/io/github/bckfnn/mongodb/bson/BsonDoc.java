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
package io.github.bckfnn.mongodb.bson;

import java.util.Date;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public interface BsonDoc extends Element {
    public BsonDoc put(String name, String value);

    public String getString(String name);

    public BsonDoc put(String name, int value);

    public int getInt(String name);

    public BsonDoc put(String name, long value);

    public long getLong(String name);

    public BsonDoc put(String name, double value);

    public double getDouble(String name);

    public BsonDoc put(String name, boolean value);

    public Number getNumber(String name);

    public boolean getBoolean(String name);

    public BsonDoc put(String name, Date value);

    public Date getDate(String name);

    public BsonDoc put(String name, byte[] value);

    public byte[] getBinary(String name);

    public BsonDoc put(String name, Pattern value);

    public Pattern getPattern(String name);

    public BsonDoc put(String name, BsonDoc value);

    public BsonDoc getDocument(String name);

    public BsonDoc put(String name, BsonArray value);

    public BsonArray getArray(String name);

    public BsonDoc putNull(String name);

    public BsonDoc putMinkey(String name);

    public BsonDoc putMaxkey(String name);

    public Set<String> getFields();

    public Element get(String name);

    public BsonDoc put(String name, Element element);

    public void decode(BsonDecoder dec);
    
    public default void putDoc(String name, Consumer<BsonDoc> handler) {
        BsonDoc doc = new BsonDocMap();
        handler.accept(doc);
        put(name, doc);
    }
    
    public default void putArray(String name, Consumer<BsonArray> handler) {
        BsonArray doc = new BsonArrayList();
        handler.accept(doc);
        put(name, doc);
    }
    
    public static BsonDoc newDoc(Consumer<BsonDoc> handler) {
        BsonDoc doc = new BsonDocMap();
        handler.accept(doc);
        return doc;
    }

}
