package io.github.bckfnn.mongodb.bson;

public class BsonJavascript implements Element {
    private String value;

    public BsonJavascript(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitJavascript(value, this);
    }
}
