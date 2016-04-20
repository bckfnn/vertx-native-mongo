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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class BsonDocMap implements BsonDoc {
    private Map<String, Element> elements = new LinkedHashMap<>();

    @Override
    public void putString(String name, String value) {
        put(name, new BsonString(value));
    }

    @Override
    public String getString(String name) {
        init();
        return get(BsonString.class, name).getValue();
    }

    @Override
    public void putInt(String name, int value) {
        put(name, new BsonInt(value));
    }

    @Override
    public int getInt(String name) {
        return get(BsonInt.class, name).getValue();
    }

    @Override
    public void putLong(String name, long value) {
        put(name, new BsonLong(value));
    }

    @Override
    public long getLong(String name) {
        return get(BsonLong.class, name).getValue();
    }

    @Override
    public void putDouble(String name, double value) {
        put(name, new BsonDouble(value));
    }

    @Override
    public double getDouble(String name) {
        return get(BsonDouble.class, name).getValue();
    }

    @Override
    public Number getNumber(String name) {
        return get(BsonNumber.class, name).getNumber();
    }

    @Override
    public void putBoolean(String name, boolean value) {
        put(name, new BsonBoolean(value));
    }

    @Override
    public boolean getBoolean(String name) {
        return get(BsonBoolean.class, name).getValue();
    }

    @Override
    public void putDate(String name, Date value) {
        put(name, new BsonDatetime(value.getTime()));
    }

    @Override
    public Date getDate(String name) {
        return new Date(get(BsonDatetime.class, name).getValue());
    }

    @Override
    public void putBinary(String name, byte[] value) {
        put(name, new BsonBinary((byte) 0, value));
    }

    @Override
    public byte[] getBinary(String name) {
        return get(BsonBinary.class, name).getValue();
    }

    @Override
    public void putPattern(String name, Pattern value) {
        put(name, new BsonRegex(value.pattern(), ""));
    }

    @Override
    public Pattern getPattern(String name) {
        return Pattern.compile(get(BsonRegex.class, name).getValue(), 0);
    }

    @Override
    public void putNull(String name) {
        put(name, new BsonNull());
    }

    @Override
    public void putMinkey(String name) {
        put(name, new BsonMinkey());
    }

    @Override
    public void putMaxkey(String name) {
        put(name, new BsonMaxkey());
    }

    @Override
    public void putDocument(String name, BsonDoc value) {
        put(name, value);
    }

    @Override
    public BsonDoc getDocument(String name) {
        return get(BsonDoc.class, name);
    }

    @Override
    public void putArray(String name, BsonArray value) {
        put(name, value);
    }

    @Override
    public BsonArray getArray(String name) {
        return get(BsonArray.class, name);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type, String name) {
        init();
        Element el = elements.get(name);
        if (el.getClass() == type || type.isAssignableFrom(el.getClass())) {
            return (T) el;
        }

        return null;
    }

    @Override
    public Element get(String name) {
        init();
        return elements.get(name);
    }

    @Override
    public void put(String name, Element element) {
        init();
        elements.put(name, element);
    }

    @Override
    public Set<String> getFields() {
        init();
        return elements.keySet();
    }

    protected void init() {

    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitDocument(name, this);
    }

    @Override
    public String toString() {
        init();
        return elements.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elements == null) ? 0 : elements.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof BsonDocMap)) {
            return false;
        }
        BsonDocMap other = (BsonDocMap) obj;
        init();
        other.init();
        if (elements == null) {
            if (other.elements != null)
                return false;
        } else if (!elements.equals(other.elements))
            return false;
        return true;
    }

    @Override
    public void decode(BsonDecoder dec) {
        dec.loadDocument(this);
    }
}
