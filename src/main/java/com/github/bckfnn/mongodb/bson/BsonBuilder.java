package com.github.bckfnn.mongodb.bson;

import java.util.Date;
import java.util.regex.Pattern;


public class BsonBuilder {
    private BsonBuilder parent;
    private Element element;
    
    public BsonBuilder(Element element) {
        this.element = element;
    }

    public BsonBuilder(BsonBuilder parent, Element element) {
        this.parent = parent;
        this.element = element;
    }

    public static BsonBuilder doc() {
        return new BsonBuilder(new BsonDocMap());
    }

    public BsonBuilder put(String name, String value) {
        topdoc().putString(name, value);
        return this;
    }

    public BsonBuilder put(String name, int value) {
        topdoc().putInt(name, value);
        return this;
    }

    public BsonBuilder put(String name, long value) {
        topdoc().putLong(name, value);
        return this;
    }

    public BsonBuilder put(String name, boolean value) {
        topdoc().putBoolean(name, value);
        return this;
    }
    
    public BsonBuilder put(String name, double value) {
        topdoc().putDouble(name, value);
        return this;
    }
    
    public BsonBuilder put(String name, Date value) {
        topdoc().putDate(name, value);
        return this;
    }
    
    public BsonBuilder put(String name, byte[] value) {
        topdoc().putBinary(name, value);
        return this;
    }
    
    public BsonBuilder put(String name, Pattern value) {
        topdoc().putPattern(name, value);
        return this;
    }
    
    public BsonBuilder putNull(String name) {
        topdoc().putNull(name);
        return this;
    }
    
    public BsonBuilder putMinKey(String name) {
        topdoc().putMinkey(name);
        return this;
    }

    public BsonBuilder putMaxKey(String name) {
        topdoc().putMaxkey(name);
        return this;
    }

    public BsonBuilder putArray(String name) {
        BsonArray arr = new BsonArray();
        topdoc().putArray(name, arr);
        return new BsonBuilder(this, arr);
    }

    public BsonBuilder putDoc(String name) {
        BsonDoc doc = new BsonDocMap();
        topdoc().putDocument(name, doc);
        return new BsonBuilder(this, doc);
    }

    public BsonBuilder append(String value) {
        toparray().add(value);
        return this;
    }

    public BsonBuilder append(int value) {
        toparray().add(value);
        return this;
    }

    public BsonBuilder append(long value) {
        toparray().add(value);
        return this;
    }

    public BsonBuilder append(double value) {
        toparray().add(value);
        return this;
    }

    public BsonBuilder append(boolean value) {
        toparray().add(value);
        return this;
    }

    public BsonBuilder appendNull() {
        toparray().addNull();
        return this;
    }
    
    public BsonBuilder appendMinkey() {
        toparray().addMinkey();
        return this;
    }

    public BsonBuilder appendMaxkey() {
        toparray().addMaxkey();
        return this;
    }

    
    public BsonBuilder appendArray() {
        BsonArray arr = new BsonArray();
        toparray().add(arr);
        return new BsonBuilder(this, arr);
    }

    public BsonBuilder appendDoc() {
        BsonDoc doc = new BsonDocMap();
        toparray().add(doc);
        return new BsonBuilder(this, doc);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get() {
        if (parent == null) {
            return (T) element;
        } else {
            return parent.get();
        }
    }
    
    public BsonDoc topdoc() {
        return (BsonDoc) element;
    }

    private BsonArray toparray() {
        return (BsonArray) element;
    }

    public BsonBuilder pop() {
        return parent;
    }

}
