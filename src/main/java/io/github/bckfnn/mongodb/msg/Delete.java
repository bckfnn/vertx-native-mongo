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
