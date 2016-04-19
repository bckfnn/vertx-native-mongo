package io.github.bckfnn.mongodb.bson;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;


//import org.vertx.java.core.buffer.Buffer;

final public class BsonEncoder implements Visitor {

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
        b.writeInt(value); //Integer.reverseBytes(value));
    }

    public void int32(int pos, int value) {
        b.setInt(pos, value);
    }

    public void int64(long value) {
        b.writeLong(value);
    }

    final ByteBuf b = Unpooled.buffer(256).order(ByteOrder.LITTLE_ENDIAN);

    public void appendByte(byte val) {
        b.writeByte(val);
    }

    public void appendBytes(byte[] val) {
        b.writeBytes(val);
    }

    public void appendString(String val) {
        appendBytes(val.getBytes(StandardCharsets.UTF_8));
    }

    public void appendInt(int val) {
        b.writeInt(val);
    }

    public void setByte(int pos, byte val) {
        b.setByte(pos, val);
    }

    public int length() {
        return b.writerIndex();
    }

    public Buffer asBuffer() {
        return Buffer.buffer(b);
    }

}
