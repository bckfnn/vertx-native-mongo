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

public interface Visitor {

    void visitArray(String name, BsonArray element);

    void visitBinary(String name, BsonBinary element);

    void visitBoolean(String name, BsonBoolean element);

    void visitDatetime(String name, BsonDatetime element);

    void visitDocument(String name, BsonDoc element);

    void visitDouble(String name, BsonDouble element);

    void visitInt(String name, BsonInt element);

    void visitJavascript(String name, BsonJavascript element);

    void visitJavascriptWithScope(String name, BsonJavascriptWithScope element);

    void visitLong(String name, BsonLong element);

    void visitNull(String name, BsonNull element);

    void visitMinkey(String name, BsonMinkey element);

    void visitMaxkey(String name, BsonMaxkey element);

    void visitObjectId(String name, BsonObjectId element);

    void visitRegex(String name, BsonRegex element);

    void visitString(String name, BsonString element);

    void visitTimestamp(String name, BsonTimestamp element);

}
