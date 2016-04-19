package io.github.bckfnn.mongodb.msg;

import io.github.bckfnn.mongodb.bson.BsonEncoder;

public class GetMore extends OutMessage {
    private String collectionName;
    private int numberToReturn;
    private long cursorId;

    /**
     * @param collectionName the collectionName to set
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     * @param numberToReturn the numberToReturn to set
     */
    public void setNumberToReturn(int numberToReturn) {
        this.numberToReturn = numberToReturn;
    }
    
    /**
     * @param cursorId the cursorId to set
     */
    public void setCursorId(long cursorId) {
        this.cursorId = cursorId;
    }

    protected int getOpcode() {
        return OP_GETMORE;
    }

    public boolean hasResponse() {
        return true;
    }

    public void doSend(BsonEncoder enc) {
        enc.int32(0); // ZERO
        enc.cstring(collectionName);
        enc.int32(numberToReturn);
        enc.int64(cursorId);
    }
}
