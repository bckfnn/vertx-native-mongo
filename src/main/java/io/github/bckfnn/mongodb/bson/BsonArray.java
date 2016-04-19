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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BsonArray implements Element, Iterable<Element> {
    private List<Element> values;

    public BsonArray(Element... elements) {
        this.values = new ArrayList<Element>();
        this.values.addAll(Arrays.asList(elements));
    }

    public List<Element> getFields() {
        return values;
    }

    public BsonArray add(Element entry) {
        values.add(entry);
        return this;
    }

    public BsonArray add(String value) {
        values.add(new BsonString(value));
        return this;
    }

    public BsonArray add(int value) {
        values.add(new BsonInt(value));
        return this;
    }

    public BsonArray add(long value) {
        values.add(new BsonLong(value));
        return this;
    }

    public BsonArray add(double value) {
        values.add(new BsonDouble(value));
        return this;
    }

    public BsonArray add(boolean value) {
        values.add(new BsonBoolean(value));
        return this;
    }

    public BsonArray addNull() {
        values.add(new BsonNull());
        return this;
    }

    public BsonArray addMinkey() {
        values.add(new BsonMinkey());
        return this;
    }

    public BsonArray addMaxkey() {
        values.add(new BsonMaxkey());
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type, int idx) {
        init();
        Element el = values.get(idx);
        if (el.getClass() == type || type.isAssignableFrom(el.getClass())) {
            return (T) el;
        }

        return null;
    }


    public String getString(int idx) {
        init();
        return get(BsonString.class, idx).getValue();
    }

    public int getInt(int idx) {
        init();
        return get(BsonInt.class, idx).getValue();
    }

    public long getLong(int idx) {
        init();
        return get(BsonLong.class, idx).getValue();
    }

    public double getDoube(int idx) {
        init();
        return get(BsonDouble.class, idx).getValue();
    }

    public boolean getBoolean(int idx) {
        init();
        return get(BsonBoolean.class, idx).getValue();
    }

    public BsonDoc getDocument(int idx) {
        init();
        return get(BsonDoc.class, idx);
    }

    public BsonArray getArray(int idx) {
        init();
        return get(BsonArray.class, idx);
    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitArray(name, this);
    }

    protected void init() {
    }

    @Override
    public Iterator<Element> iterator() {
        init();
        return values.iterator();
    }

    @Override
    public String toString() {
        init();
        return values.toString();
    }
}
