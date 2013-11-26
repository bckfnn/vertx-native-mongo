package com.github.bckfnn.mongodb.bson;

import org.vertx.java.core.buffer.Buffer;

public class BsonDecoder {
    private Buffer buffer;
    private int pos = 0;

    public BsonDecoder(Buffer buffer) {
        this.buffer = buffer;
        this.pos = 0;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
    
    public static BsonDoc decode(Buffer buffer) {
        BsonDoc doc = new BsonDocMap();
        new BsonDecoder(buffer).loadDocument(doc);
        return doc;
    }
    
    public Element readElement(int type) {
        int size;

        switch ((byte) type) {
        case Element.DOUBLE:
            return new BsonDouble(Double.longBitsToDouble(int64()));

        case Element.STRING:
            return new BsonString(string());

        case Element.DOCUMENT:
            size = int32() - 4;
            BsonDoc doc = new BsonDocLazy(this, pos - 4);
            pos += size;
            return doc;

        case Element.ARRAY:
            size = int32() - 4;
            return new BsonArrayLazy(buffer.getBuffer(pos, pos += size));

        case Element.BINARY:
            size = int32();
            int subtype = int8();
            return new BsonBinary((byte) subtype, buffer.getBytes(pos, pos += size));

        case Element.OBJECTID:
            return new BsonObjectId(int32(), int64());

        case Element.BOOLEAN:
            return new BsonBoolean(int8() == 1);

        case Element.TIMESTAMP:
            return new BsonTimestamp(int32(), int32());

        case Element.NULL:
            return new BsonNull();

        case Element.REGEX:
            return new BsonRegex(cstring(), cstring());

        case Element.JAVASCRIPT:
            return new BsonJavascript(string());

        case Element.JAVASCRIPT_WITH_SCOPE:
            return new BsonJavascriptWithScope(string(), (BsonDoc) readElement(Element.DOCUMENT));

        case Element.INT32:
            return new BsonInt(int32());

        case Element.INT64:
            return new BsonLong(int64());
            
        case Element.MINKEY:
            return new BsonMinkey();

        case Element.MAXKEY:
            return new BsonMaxkey();

        case Element.DATETIME:
            return new BsonDatetime(int64());

        default:
            /*
            DBPOINTER(0x0C),
            */
        }
        throw new RuntimeException("Unknown bson element type:" + type);
    }

    public void loadDocument(BsonDoc doc) {
        int size = int32() - 4;
        for (int end = pos + size; pos < end; ) {
            int type = int8();
            if (type == 0) {
                break;
            }
            String name = ename();
            //System.out.println(type + " " + name);
            Element element = readElement(type);
            doc.put(name, element);
        }
    }

    public void loadArray(BsonArray arr) {
        //System.out.println("array size:" + buffer.length());
        for (int l = buffer.length(); pos < l;) {
            int type = int8();
            if (type == 0) {
                break;
            }
            ename();
            //System.out.println(type + " " + name);
            Element element = readElement(type);
            arr.add(element);
        }
    }

    private String ename() {
        int start = pos;
        while (buffer.getByte(pos++) != 0)
            ;
        return buffer.getString(start, pos - 1, "UTF-8");
    }

    private String cstring() {
        return ename();
    }

    private String string() {
        int size = int32();
        String value = buffer.getString(pos, pos + size - 1, "UTF-8");
        pos += size;
        return value;
    }

    public int int8() {
        return buffer.getByte(pos++) & 0xFF;
    }

    public int int32() {
        int l = Integer.reverseBytes(buffer.getInt(pos));
        pos += 4;
        return l;
        /*
        return buffer.getByte(pos++) & 0xFF |
                (buffer.getByte(pos++) & 0xFF) << 8 |
                (buffer.getByte(pos++) & 0xFF) << 16 |
                (buffer.getByte(pos++) & 0xFF) << 24;
        */
    }

    public long int64() {
        long l = Long.reverseBytes(buffer.getLong(pos));
        pos += 8;
        return l;
        /*
        return buffer.getByte(pos++) & 0xFF |
                (buffer.getByte(pos++) & 0xFF) << 8 |
                (buffer.getByte(pos++) & 0xFF) << 16 |
                (buffer.getByte(pos++) & 0xFF) << 24 |
                (buffer.getByte(pos++) & 0xFF) << 32 |
                (buffer.getByte(pos++) & 0xFF) << 40 |
                (buffer.getByte(pos++) & 0xFF) << 48 |
                (buffer.getByte(pos++) & 0xFF) << 56;
        */
    }
}
