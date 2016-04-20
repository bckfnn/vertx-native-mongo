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

import java.util.List;
import java.util.function.Consumer;

public interface BsonArray extends Element, Iterable<Element> {

    public List<Element> getFields();

    public BsonArray add(Element entry);

    public BsonArray add(String value);

    public BsonArray add(int value);

    public BsonArray add(long value);

    public BsonArray add(double value);

    public BsonArray add(boolean value);

    public BsonArray addNull();

    public BsonArray addMinkey();

    public BsonArray addMaxkey();

    public <T> T get(Class<T> type, int idx);

    public String getString(int idx);

    public int getInt(int idx);

    public long getLong(int idx);

    public double getDoube(int idx);

    public boolean getBoolean(int idx);

    public BsonDoc getDocument(int idx);

    public BsonArray getArray(int idx);

    public void decode(BsonDecoder dec);
    
    public default void addDoc(Consumer<BsonDoc> handler) {
        BsonDoc doc = new BsonDocMap();
        handler.accept(doc);
        add(doc);
    }
    
    public default void addArray(Consumer<BsonArray> handler) {
        BsonArray doc = new BsonArrayList();
        handler.accept(doc);
        add(doc);
    }
    
    public static BsonArray newArray(Consumer<BsonArray> handler) {
        BsonArray arr = new BsonArrayList();
        handler.accept(arr);
        return arr;
    }

}
