package io.github.bckfnn.mongodb;

import java.util.function.Consumer;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface Callback<T> {
    public void call(T result, Throwable error);

    default <R> Callback<R> call(Consumer<R> handler) {
        return (t, e) -> {
            if (e != null) {
                call(null, e);
            } else {
                handler.accept(t);
            }
        };
    }

    default <R> Handler<AsyncResult<R>> handler(Consumer<R> handler) {
        return ar -> {
            if (ar.failed()) {
                call(null, ar.cause());
            } else {
                handler.accept(ar.result());
            }
        };
    }

    default void ok(T value) {
        call(value, null);
    }

    default void ok() {
        call(null, null);
    }


    default void fail(Throwable error) {
        call(null, error);
    }

}