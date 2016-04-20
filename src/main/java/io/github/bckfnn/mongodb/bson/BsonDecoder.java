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

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;

final public class BsonDecoder {
    private ByteBuf buffer;
    private int pos = 0;

    public BsonDecoder(Buffer buffer) {
        this.buffer = buffer.getByteBuf().order(ByteOrder.LITTLE_ENDIAN);
        this.pos = 0;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getPos() {
        return pos;
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
            BsonDoc doc = new BsonDocMap();
            loadDocument(doc);
            return doc;

        case Element.ARRAY:
            BsonArray arr = new BsonArrayList();
            arr.decode(this);
            return arr;

        case Element.BINARY:
            size = int32();
            int subtype = int8();
            byte[] bytes = new byte[size];
            buffer.getBytes(pos, bytes, 0, size);
            BsonBinary bin = new BsonBinary((byte) subtype, bytes);
            pos += size;
            return bin;

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
        throw new RuntimeException("Unknown bson element type:" + type + " at " + pos);
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
        int size = int32() - 4;
        //System.out.println("array size:" + buffer.length());
        for (int end = pos + size; pos < end; ) {
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
        return buffer.toString(start, pos - start - 1, StandardCharsets.UTF_8);
    }

    private String cstring() {
        return ename();
    }

    private String string() {
        int size = int32();
        String value = buffer.toString(pos, size - 1, StandardCharsets.UTF_8);
        pos += size;
        return value;
    }

    public int int8() {
        return buffer.getByte(pos++) & 0xFF;
    }

    public int int32() {
        int l = buffer.getInt(pos);
        pos += 4;
        return l;
    }

    public long int64() {
        long l = buffer.getLong(pos);
        pos += 8;
        return l;
    }
}
