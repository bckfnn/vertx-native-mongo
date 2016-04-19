package com.github.bckfnn.mongodb.bson;

import io.vertx.core.buffer.Buffer;

public class BsonArrayLazy extends BsonArray {
    private Buffer buffer;

    public BsonArrayLazy(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    protected void init() {
        if (buffer == null) {
            return;
        }
        Buffer b = buffer;
        buffer = null;

        BsonDecoder dec = new BsonDecoder(b);
        dec.loadArray(this);
    }

}
