package io.github.bckfnn.mongodb;

import io.github.bckfnn.mongodb.msg.OutMessage;
import io.github.bckfnn.mongodb.msg.Reply;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface Connection {
    public void open(final Handler<AsyncResult<Connection>> handler);

    public void close(final Handler<Void> handler);

    public void write(final OutMessage msg, OutMessage lastError, final Handler<AsyncResult<Reply>> handler);
}
