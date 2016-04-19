package com.github.bckfnn.mongodb.bson;

import io.vertx.core.buffer.Buffer;

public class JsonEncoder implements Visitor {
    private Buffer buffer;

    public JsonEncoder(Buffer buffer) {
        this.buffer = buffer;
    }

    public Buffer getBuffer() {
        return buffer;
    }

    public static Buffer encode(BsonDoc doc) {
        Buffer buf = Buffer.buffer();
        new JsonEncoder(buf).visitDocument(doc);
        return buf;
    }


    @Override
    public void visitArray(String name, BsonArray element) {
        ename(name);
        buffer.appendByte((byte) '[');
        boolean comma = false;
        for (Element elm : element.getFields()) {
            if (comma) {
                buffer.appendByte((byte) ',');
            }
            elm.accept(null, this);
            comma = true;
        }
        buffer.appendByte((byte) ']');
    }

    @Override
    public void visitBinary(String name, BsonBinary element) {
        ename(name);
        buffer.appendBytes(element.getValue());
    }

    @Override
    public void visitBoolean(String name, BsonBoolean element) {
        ename(name);
        buffer.appendString(element.getValue() ? "true" : "false");
    }

    @Override
    public void visitDatetime(String name, BsonDatetime element) {
        ename(name);
        // TODO format as UTC date
        buffer.appendString(Long.toString(element.getValue()));
    }

    @Override
    public void visitDocument(String name, BsonDoc element) {
        ename(name);
        visitDocument(element);
    }

    @Override
    public void visitDouble(String name, BsonDouble element) {
        ename(name);
        buffer.appendString(Double.toString(element.getValue()));
    }

    @Override
    public void visitInt(String name, BsonInt element) {
        ename(name);
        buffer.appendString(Integer.toString(element.getValue()));
    }

    @Override
    public void visitJavascript(String name, BsonJavascript element) {
        ename(name);
        string(element.getValue());
    }

    @Override
    public void visitJavascriptWithScope(String name, BsonJavascriptWithScope element) {
        ename(name);
        string(element.getValue());
        visitDocument(element.getScope());
    }

    @Override
    public void visitLong(String name, BsonLong element) {
        ename(name);
        buffer.appendString(Long.toString(element.getValue()));
    }

    @Override
    public void visitMaxkey(String name, BsonMaxkey element) {
        ename(name);
        buffer.appendString("1");
    }

    @Override
    public void visitMinkey(String name, BsonMinkey element) {
        ename(name);
        buffer.appendString("-1");
    }

    @Override
    public void visitNull(String name, BsonNull element) {
        ename(name);
        buffer.appendString("null");
    }

    @Override
    public void visitObjectId(String name, BsonObjectId element) {
        ename(name);
        buffer.appendString(Integer.toString(element.timestamp));
        buffer.appendString(Long.toString(element.machineId));
    }

    @Override
    public void visitRegex(String name, BsonRegex element) {
        ename(name);
        string(element.getValue());
        string(element.getOptions());
    }

    @Override
    public void visitString(String name, BsonString element) {
        ename(name);
        string(element.getValue());
    }

    @Override
    public void visitTimestamp(String name, BsonTimestamp element) {
        ename(name);
        // TODO format as timestamp
        buffer.appendString(element.toString());
    }

    public void visitDocument(BsonDoc element) {
        buffer.appendByte((byte) '{');
        boolean comma = false;
        for (String fname : element.getFields()) {
            if (comma) {
                buffer.appendByte((byte) ',');
            }
            element.get(fname).accept(fname, this);
            comma = true;
        }
        buffer.appendByte((byte) '}');
    }

    private void ename(String name) {
        if (name != null) {
            buffer.appendByte((byte) '"');
            buffer.appendString(name, "UTF-8");
            buffer.appendByte((byte) '"');
            buffer.appendByte((byte) ':');
        }
    }

    private void string(String value) {
        buffer.appendByte((byte) '"');
        buffer.appendString(value, "UTF-8");
        buffer.appendByte((byte) '"');
    }
}
