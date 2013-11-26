package com.github.bckfnn.mongodb;

import org.vertx.java.core.Handler;

import com.github.bckfnn.mongodb.msg.OutMessage;
import com.github.bckfnn.mongodb.msg.Reply;


public interface Connection {
    public void open(final Callback<Connection> callback);

    public void close(final Handler<Void> handler);

    public void write(final OutMessage msg, OutMessage lastError, final Callback<Reply> handler);
}
