package io.github.bckfnn.mongodb;

import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.msg.Reply;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;


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

    @Override
    public String toString() {
        return lastError.toString();
    }

    public static Handler<AsyncResult<Reply>> result(final Handler<AsyncResult<WriteResult>> handler) {
        return Utils.handler(handler, reply -> {
            handler.handle(Future.succeededFuture(new WriteResult(reply.getDocuments().get(0))));
        });
    }
}
