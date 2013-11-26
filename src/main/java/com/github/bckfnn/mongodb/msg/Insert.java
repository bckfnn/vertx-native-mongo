package com.github.bckfnn.mongodb.msg;

import java.util.List;

import com.github.bckfnn.mongodb.bson.BsonDoc;
import com.github.bckfnn.mongodb.bson.BsonEncoder;


public class Insert extends OutMessage {
    private int flags;
    private String collectionName;
    private List<BsonDoc> documents;

    /**
     * @param collectionName the collectionName to set
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     * @param documents the documents to set
     */
    public void setDocuments(List<BsonDoc> documents) {
        this.documents = documents;
    }

    /**
     * @param upsert set the Upsert flag.
     */
    public void setUpsert(boolean upsert) {
        flags = flag(0, flags, upsert);
    }

    /**
     * @param multiUpdate set the MultiUpdate flag.
     */
    public void setMultiUpdate(boolean multiUpdate) {
        flags = flag(1, flags, multiUpdate);
    }

    @Override
    protected void doSend(BsonEncoder enc) {
        enc.int32(flags);
        enc.cstring(collectionName);
        for (BsonDoc doc : documents) {
            enc.visitDocument(doc);
        }
    }

    @Override
    protected int getOpcode() {
        return OP_INSERT;
    }
}
