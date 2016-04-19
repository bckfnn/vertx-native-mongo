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
