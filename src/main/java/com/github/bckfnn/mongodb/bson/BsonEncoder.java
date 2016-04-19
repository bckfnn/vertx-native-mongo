package com.github.bckfnn.mongodb.bson;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;


//import org.vertx.java.core.buffer.Buffer;

public class BsonEncoder implements Visitor {

    public BsonEncoder() {
    }


    public static Buffer encode(BsonDoc doc) {
        BsonEncoder enc = new BsonEncoder();
        enc.visitDocument(doc);
        return enc.asBuffer();
    }

    public Buffer getBuffer() {
        return asBuffer();
    }


    @Override
    public void visitArray(String name, BsonArray element) {
        appendByte(Element.ARRAY);
        ename(name);

        int pos = length();
        int32(0);
        int i = 0;
        for (Element elm : element.getFields()) {
            elm.accept(String.valueOf(i++), this);
        }
        appendByte((byte) 0);
        int32(pos, length() - pos);
    }

    @Override
    public void visitBinary(String name, BsonBinary element) {
        appendByte(Element.BINARY);
        ename(name);
        int32(element.getValue().length);
        appendByte(element.getSubtype());
        appendBytes(element.getValue());
    }

    @Override
    public void visitBoolean(String name, BsonBoolean element) {
        appendByte(Element.BOOLEAN);
        ename(name);
        appendByte(element.getValue() ? (byte) 1 : (byte) 0);
    }

    @Override
    public void visitDatetime(String name, BsonDatetime element) {
        appendByte(Element.DATETIME);
        ename(name);
        int64(element.getValue());
    }

    @Override
    public void visitDocument(String name, BsonDoc element) {
        appendByte(Element.DOCUMENT);
        ename(name);

        visitDocument(element);
    }

    @Override
    public void visitDouble(String name, BsonDouble element) {
        appendByte(Element.DOUBLE);
        ename(name);
        int64(Double.doubleToLongBits(element.getValue()));
    }

    @Override
    public void visitInt(String name, BsonInt element) {
        appendByte(Element.INT32);
        ename(name);
        int32(element.getValue());
    }

    @Override
    public void visitJavascript(String name, BsonJavascript element) {
        appendByte(Element.JAVASCRIPT);
        ename(name);
        string(element.getValue());
    }

    @Override
    public void visitJavascriptWithScope(String name, BsonJavascriptWithScope element) {
        appendByte(Element.JAVASCRIPT_WITH_SCOPE);
        ename(name);
        string(element.getValue());
        visitDocument(element.getScope());
    }

    @Override
    public void visitLong(String name, BsonLong element) {
        appendByte(Element.INT64);
        ename(name);
        int64(element.getValue());
    }

    @Override
    public void visitMaxkey(String name, BsonMaxkey element) {
        appendByte(Element.MAXKEY);
        ename(name);
    }

    @Override
    public void visitMinkey(String name, BsonMinkey element) {
        appendByte(Element.MINKEY);
        ename(name);
    }

    @Override
    public void visitNull(String name, BsonNull element) {
        appendByte(Element.NULL);
        ename(name);
    }

    @Override
    public void visitObjectId(String name, BsonObjectId element) {
        appendByte(Element.OBJECTID);
        ename(name);
        int32(element.timestamp);
        int64(element.machineId);
    }

    @Override
    public void visitRegex(String name, BsonRegex element) {
        appendByte(Element.REGEX);
        ename(name);
        cstring(element.getValue());
        cstring(element.getOptions());
    }

    @Override
    public void visitString(String name, BsonString element) {
        appendByte(Element.STRING);
        ename(name);
        string(element.getValue());
    }

    @Override
    public void visitTimestamp(String name, BsonTimestamp element) {
        appendByte(Element.TIMESTAMP);
        ename(name);
        int32(element.getIncrement());
        int32(element.getTimestamp());
    }

    public void visitDocument(BsonDoc element) {
        int pos = length();
        int32(0);
        if (element != null) {
            for (String fname : element.getFields()) {
                element.get(fname).accept(fname, this);
            }
        }
        appendByte((byte) 0);
        int32(pos, length() - pos);
    }

    private void ename(String name) {
        appendString(name);
        appendByte((byte) 0);
    }

    public void cstring(String name) {
        appendString(name);
        appendByte((byte) 0);
    }

    private void string(String value) {
        int32(value.length() + 1);
        appendString(value);
        appendByte((byte) 0);
    }

    public void int32(int value) {
        appendInt(value); //Integer.reverseBytes(value));
        /*
        buffer.appendByte((byte) (value & 0xFF));
        buffer.appendByte((byte) (value >> 8 & 0xFF));
        buffer.appendByte((byte) (value >> 16 & 0xFF));
        buffer.appendByte((byte) (value >> 24 & 0xFF));
         */
    }

    public void int32(int pos, int value) {
        setByte(pos++, (byte) (value & 0xFF));
        setByte(pos++, (byte) (value >> 8 & 0xFF));
        setByte(pos++, (byte) (value >> 16 & 0xFF));
        setByte(pos++, (byte) (value >> 24 & 0xFF));
    }

    public void int64(long value) {
        appendByte((byte) (value & 0xFF));
        appendByte((byte) (value >> 8 & 0xFF));
        appendByte((byte) (value >> 16 & 0xFF));
        appendByte((byte) (value >> 24 & 0xFF));
        appendByte((byte) (value >> 32 & 0xFF));
        appendByte((byte) (value >> 40 & 0xFF));
        appendByte((byte) (value >> 48 & 0xFF));
        appendByte((byte) (value >> 56 & 0xFF));
    }

    int pos;
    byte[] buffer = new byte[512];

    public void appendByte(byte val) {
        ensureLength(1);
        buffer[pos++] = val;
    }

    public void appendBytes(byte[] val) {
        int len = val.length;
        ensureLength(len);
        System.arraycopy(val,  0, buffer, pos, len);
        pos += len;
    }

    public void appendString(String val) {
        try {
            appendBytes(val.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void appendInt(int val) {
        ensureLength(4);

        buffer[pos++] = (byte) (val >> 0);
        buffer[pos++] = (byte) (val >> 8);
        buffer[pos++] = (byte) (val >> 16);
        buffer[pos++] = (byte) (val >> 24);
    }

    public void setByte(int pos, byte val) {
        buffer[pos] = val;
    }

    public int length() {
        return pos;
    }

    private void ensureLength(int len) {
        if (pos + len > buffer.length) {
            byte[] b = buffer;
            buffer = new byte[buffer.length + 128];
            System.arraycopy(b, 0, buffer, 0, pos);
        }
    }

    public Buffer asBuffer() {
        return Buffer.buffer(Unpooled.wrappedBuffer(buffer, 0, pos));
    }

}
