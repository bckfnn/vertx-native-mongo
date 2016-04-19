package io.github.bckfnn.mongodb.bson;

import java.util.Date;

public class BsonTimestamp implements Element {
    private int increment;
    private int timestamp;

    public BsonTimestamp(int increment, int timestamp) {
        this.increment = increment;
        this.timestamp = timestamp;
    }

    public int getIncrement() {
        return increment;
    }

    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitTimestamp(name, this);
    }

    @Override
    public String toString() {
        return new Date(timestamp * 1000L).toString() + ":" + increment;
    }
}
