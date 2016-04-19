/*
 * Copyright 2016 Finn Bock
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bckfnn.mongodb.msg;

import io.github.bckfnn.mongodb.bson.BsonEncoder;

public abstract class OutMessage {
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
    protected int opcode;

    /**
     * @return the requestId
     */
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    protected abstract void doSend(BsonEncoder enc);

    protected abstract int getOpcode();

    public boolean hasResponse() {
        return true;
    }

    public void send(BsonEncoder enc) {
        int pos = enc.length();

        enc.int32(0); // size
        enc.int32(requestId); // requestId
        enc.int32(0); // responseId
        enc.int32(getOpcode()); // opcode

        doSend(enc);

        enc.int32(pos, enc.length() - pos);
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
