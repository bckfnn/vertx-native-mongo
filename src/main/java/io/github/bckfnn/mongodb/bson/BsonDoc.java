package io.github.bckfnn.mongodb.bson;

import java.util.Date;
import java.util.Set;
import java.util.regex.Pattern;

public interface BsonDoc extends Element {
    public void putString(String name, String value);

    public String getString(String name);

    public void putInt(String name, int value);

    public int getInt(String name);

    public void putLong(String name, long value);

    public long getLong(String name);

    public void putDouble(String name, double value);

    public double getDouble(String name);

    public void putBoolean(String name, boolean value);

    public boolean getBoolean(String name);

    public void putDate(String name, Date value);

    public Date getDate(String name);

    public void putBinary(String name, byte[] value);

    public byte[] getBinary(String name);

    public void putPattern(String name, Pattern value);

    public Pattern getPattern(String name);

    public void putDocument(String name, BsonDoc value);

    public BsonDoc getDocument(String name);

    public void putArray(String name, BsonArray value);

    public BsonArray getArray(String name);

    public void putNull(String name);

    public void putMinkey(String name);
    
    public void putMaxkey(String name);
    
    public Set<String> getFields();

    public Element get(String name);

    public void put(String name, Element element);
}
