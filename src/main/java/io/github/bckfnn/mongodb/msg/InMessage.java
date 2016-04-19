package io.github.bckfnn.mongodb.msg;

import io.github.bckfnn.mongodb.bson.BsonDecoder;

public abstract class InMessage {
    protected static final int OP_REPLY = 1; // Reply to a client request. responseTo is set
    protected static final int OP_MSG = 1000; // generic msg command followed by a string
    protected static final int OP_UPDATE = 2001; // update document
    protected static final int OP_INSERT = 2002; // insert new document
    protected static final int RESERVED = 2003; // formerly used for OP_GET_BY_OID
    protected static final int OP_QUERY = 2004; // query a collection
    protected static final int OP_GETMORE = 2005; // Get more data from a query. See Cursors
    protected static final int OP_DELETE = 2006; // Delete documents
    protected static final int OP_KILL_CURSORS = 2007; // Tell database client is done with a cursor  

    protected int size;
    protected int requestId;
    protected int responseId;
    protected int opcode;

    public int getResponseId() {
        return responseId;
    }

    protected void read(BsonDecoder dec) {
        //size = dec.int32();
        requestId = dec.int32();
        responseId = dec.int32();
        opcode = dec.int32();

        //System.out.println("size:" + size);
        //System.out.println("requestId:" + requestId);
        //System.out.println("responseId:" + responseId);
        //System.out.println("opcode:" + opcode);
    }

    public int flag(int bit, int flag, boolean set) {
        if (set) {
            return flag | 1 << bit;
        } else {
            return flag & ~(1 << bit);
        }
    }

    public boolean isSet(int flag, int bit) {
        return (flag & (1 << bit)) != 0;
    }
}
