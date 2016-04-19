package io.github.bckfnn.mongodb.msg;

import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.bson.BsonEncoder;

public class Query extends OutMessage {
    private int flags;
    private String collectionName;
    private int numberToSkip;
    private int numberToReturn;
    private BsonDoc selector;
    private BsonDoc returnFieldSelector;

    public void setTailableCursor(boolean tailableCursor) {
        flags = flag(1, flags, tailableCursor);
    }

    public void setSlaveOk(boolean slaveOk) {
        flags = flag(2, flags, slaveOk);
    }

    public void setNoCursorTimeout(boolean noCursorTimeout) {
        flags = flag(4, flags, noCursorTimeout);
    }

    public void setAwaitData(boolean awaitData) {
        flags = flag(5, flags, awaitData);
    }

    public void setExhaust(boolean exhaust) {
        flags = flag(6, flags, exhaust);
    }

    public void setPartial(boolean partial) {
        flags = flag(7, flags, partial);
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     * @param numberToSkip the numberToSkip to set
     */
    public void setNumberToSkip(int numberToSkip) {
        this.numberToSkip = numberToSkip;
    }

    /**
     * @param numberToReturn the numberToReturn to set
     */
    public void setNumberToReturn(int numberToReturn) {
        this.numberToReturn = numberToReturn;
    }

    /**
     * @param selector the selector to set
     */
    public void setSelector(BsonDoc selector) {
        this.selector = selector;
    }
    
    /**
     * @param returnFieldSelector the returnFieldSelector to set
     */
    public void setReturnFieldSelector(BsonDoc returnFieldSelector) {
        this.returnFieldSelector = returnFieldSelector;
    }

    protected int getOpcode() {
        return OP_QUERY;
    }

    public boolean hasResponse() {
        return true;
    }

    public void doSend(BsonEncoder enc) {
        enc.int32(flags); // flags
        enc.cstring(collectionName);
        enc.int32(numberToSkip); //skip
        enc.int32(numberToReturn); //return
        enc.visitDocument(selector);
        if (returnFieldSelector != null) {
        	enc.visitDocument(returnFieldSelector);
        }
    }


    public static String hex(byte[] data) {
        StringBuilder sb = new StringBuilder();

        for (byte b : data) {
            if (b >= ' ' && b <= 127) {
                sb.append((char) b);
            } else {
                String s = Integer.toHexString(b & 0xFF);
                sb.append("%" + (s.length() == 1 ? "0" : "") + s);
            }
        }
        return sb.toString();
    }
}
