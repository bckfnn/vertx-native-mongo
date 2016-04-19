package io.github.bckfnn.mongodb.bson;

public class BsonObjectId implements Element {
    public int timestamp;
    public long machineId;

    public BsonObjectId(int timestamp, long machineId) {
        this.timestamp = timestamp;
        this.machineId = machineId;
    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitObjectId(name, this);
    }
}
