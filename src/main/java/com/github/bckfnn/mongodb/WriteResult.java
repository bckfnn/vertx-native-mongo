package com.github.bckfnn.mongodb;

import com.github.bckfnn.mongodb.bson.BsonDoc;
import com.github.bckfnn.mongodb.msg.Reply;


public class WriteResult {
    private BsonDoc lastError;

    public WriteResult(BsonDoc lastError) {
        this.lastError = lastError;
    }

    public int getN() {
        return lastError.getInt("n");
    }

    public String getError() {
        return lastError.getString("err");
    }

    public double getOk() {
        return lastError.getDouble("ok");
    }

    public String toString() {
        return lastError.toString();
    }

    public static Callback<Reply> result(final Callback<WriteResult> callback) {
        return new Callback.Default<Reply>(callback) {
            @Override
            public void handle(Reply reply) {
                callback.handle(new WriteResult(reply.getDocuments().get(0)));
            }
        };
    }
}
