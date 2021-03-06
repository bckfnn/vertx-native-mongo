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
