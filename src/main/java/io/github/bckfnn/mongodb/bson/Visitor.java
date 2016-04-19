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
