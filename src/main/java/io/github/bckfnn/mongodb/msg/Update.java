package io.github.bckfnn.mongodb.msg;

import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.bson.BsonEncoder;

public class Update extends OutMessage {
    private int flags;
    private String collectionName;
    private BsonDoc selector;
    private BsonDoc update;

    public void setUpsert(boolean upsert) {
        flags = flag(0, flags, upsert);
    }

    public void setMultiUpdate(boolean multiUpdate) {
        flags = flag(1, flags, multiUpdate);
    }

    @Override
    protected void doSend(BsonEncoder enc) {
    	enc.int32(0); // ZERO
    	enc.cstring(collectionName);
    	enc.int32(flags);
    	enc.visitDocument(selector);
    	enc.visitDocument(update);
    }

    @Override
    protected int getOpcode() {
        return OP_UPDATE;
    }
}
