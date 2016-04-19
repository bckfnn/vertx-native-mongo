package io.github.bckfnn.mongodb.bson;

public class BsonJavascriptWithScope implements Element {
    private String value;
    private BsonDoc scope;

    public BsonJavascriptWithScope(String value, BsonDoc scope) {
        this.value = value;
        this.scope = scope;
    }

    public String getValue() {
        return value;
    }

    public BsonDoc getScope() {
        return scope;
    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitJavascriptWithScope(name, this);
    }
}
