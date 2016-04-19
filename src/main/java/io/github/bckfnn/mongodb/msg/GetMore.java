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

    @Override
    protected int getOpcode() {
        return OP_GETMORE;
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public void doSend(BsonEncoder enc) {
        enc.int32(0); // ZERO
        enc.cstring(collectionName);
        enc.int32(numberToReturn);
        enc.int64(cursorId);
    }
}
