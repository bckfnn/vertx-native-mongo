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

import java.util.ArrayList;
import java.util.List;

import io.github.bckfnn.mongodb.bson.BsonDecoder;
import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.bson.BsonDocMap;


public class Reply extends InMessage {
    private int responseFlags; // bit vector - see details below
    private long cursorID; // cursor id if client needs to do get more's
    private int startingFrom; // where in the cursor this reply is starting
    private int numberReturned; // number of documents in the reply
    private List<BsonDoc> documents;

    /**
     * @return the responseFlags
     */
    public int getResponseFlags() {
        return responseFlags;
    }

    /**
     * @return the CursorNotFound flag
     */
    public boolean isCursorNotFound() {
        return isSet(responseFlags, 0);
    }

    /**
     * @return the QueryFailure flag
     */
    public boolean isQueryFailure() {
        return isSet(responseFlags, 1);
    }

    /**
     * @return the ShardConfigStale flag
     */
    public boolean isShardConfigStale() {
        return isSet(responseFlags, 2);
    }

    /**
     * @return the AwaitCapable flag
     */
    public boolean isAwaitCapable() {
        return isSet(responseFlags, 3);
    }

    /**
     * @return the cursorID
     */
    public long getCursorID() {
        return cursorID;
    }

    /**
     * @return the startingFrom
     */
    public int getStartingFrom() {
        return startingFrom;
    }

    /**
     * @return the numberReturned
     */
    public int getNumberReturned() {
        return numberReturned;
    }

    /**
     * @return the documents
     */
    public List<BsonDoc> getDocuments() {
        return documents;
    }

    @Override
    public void read(BsonDecoder dec) {
        super.read(dec);
        responseFlags = dec.int32();
        cursorID = dec.int64();
        startingFrom = dec.int32();
        numberReturned = dec.int32();
        //System.out.println("cursor:" + cursorID + " start:" + startingFrom + " returned:" + numberReturned);

        documents = new ArrayList<>();
        for (int i = 0; i < numberReturned; i++) {
            //dec.int32();
            //System.out.println("doc size:" + dec.int32());
            BsonDoc doc = new BsonDocMap();
            dec.loadDocument(doc);
            documents.add(doc);
        }
    }

}
