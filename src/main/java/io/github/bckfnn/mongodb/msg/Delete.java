package io.github.bckfnn.mongodb.msg;

import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.bson.BsonEncoder;

public class Delete extends OutMessage {
    private int flags;
    private String collectionName;
    private BsonDoc selector;

    /**
     * @param collectionName the collectionName to set
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     * @param selector the selector to set
     */
    public void setSelector(BsonDoc selector) {
        this.selector = selector;
    }

    /**
     * @param singleRemove set the SingleRemove flag.
     */
    public void setSingleRemove(boolean singleRemove) {
        flags = flag(0, flags, singleRemove);
    }

    @Override
    protected void doSend(BsonEncoder enc) {
        enc.int32(0); // ZERO
        enc.cstring(collectionName);
        enc.int32(flags);
        enc.visitDocument(selector);
    }

    @Override
    protected int getOpcode() {
        return OP_DELETE;
    }
}
