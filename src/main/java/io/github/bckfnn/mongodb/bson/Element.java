package io.github.bckfnn.mongodb.bson;

public interface Element {

    public final static byte DOUBLE = 0x01;
    public final static byte STRING = 0x02;
    public final static byte DOCUMENT = 0x03;
    public final static byte ARRAY = 0x04;
    public final static byte BINARY = 0x05;
    public final static byte OBJECTID = 0x07;
    public final static byte BOOLEAN = 0x08;
    public final static byte DATETIME = 0x09;
    public final static byte NULL = 0x0A;
    public final static byte REGEX = 0x0B;
    public final static byte JAVASCRIPT = 0x0D;
    public final static byte JAVASCRIPT_WITH_SCOPE = 0x0F;
    public final static byte INT32 = 0x10;
    public final static byte TIMESTAMP = 0x11;
    public final static byte INT64 = 0x12;
    public final static byte MINKEY = (byte) 0xFF;
    public final static byte MAXKEY = 0x7F;

    public void accept(String name, Visitor visitor);
}
