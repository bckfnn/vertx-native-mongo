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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

public class Utils {

    public static <T, R> Handler<AsyncResult<T>> handler(Handler<AsyncResult<R>> handler, Consumer<T> func) {
        return ar -> {
            //System.out.println("got handler callback " + ar.succeeded());
            if (ar.succeeded()) {
                func.accept(ar.result());
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        };
    }

    public static <T, R> Handler<AsyncResult<Cursor<R>>> one(Handler<AsyncResult<T>> handler, Consumer<R> func) {
        return ar -> {
            if (ar.failed()) {
                handler.handle(Future.failedFuture(ar.cause()));
            } else {
                ar.result().exceptionHandler(e -> {
                    handler.handle(Future.failedFuture(e));
                });
                ar.result().handler(data -> {
                    func.accept(data);
                });
            }
        };
    }

    public static <T> void collect(AsyncResult<Cursor<T>> rs, Handler<AsyncResult<List<T>>> handler) {
        ArrayList<T> list = new ArrayList<>();
        rs.result().endHandler($ -> handler.handle(Future.succeededFuture(list)));
        rs.result().exceptionHandler(t -> handler.handle(Future.failedFuture(t)));
        rs.result().handler(data -> list.add(data));
    }

    public static <T, R> Handler<AsyncResult<Cursor<R>>> each(Handler<AsyncResult<Cursor<T>>> handler, BiConsumer<Consumer<T>, R> consumer) {
        ElementReadStream<T> rs = new ElementReadStream<T>();
        return ar -> {
            if (ar.failed()) {
                handler.handle(Future.failedFuture(ar.cause()));
            } else {
                ar.result().exceptionHandler(e -> {
                    rs.fail(e);
                });
                ar.result().endHandler($ -> {
                    rs.end();
                });
                ar.result().handler(data -> {
                    consumer.accept(result -> rs.send(result), data);
                });
            }
        };
    }


    public static class ElementReadStream<T> implements ReadStream<T> {
        private LinkedList<T> elements = new LinkedList<T>();
        private Handler<Throwable> exceptionHandler;
        private Handler<T> dataHandler;
        private Handler<Void> endHandler;
        private boolean pause = true;
        private boolean endPending = false;

        @Override
        public ReadStream<T> exceptionHandler(Handler<Throwable> exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        @Override
        public ReadStream<T> handler(Handler<T> dataHandler) {
            this.dataHandler = dataHandler;
            resume();
            return this;
        }

        @Override
        public ReadStream<T> endHandler(Handler<Void> endHandler) {
            this.endHandler = endHandler;
            return this;
        }

        @Override
        public ReadStream<T> pause() {
            pause = true;
            emit();
            return this;
        }

        @Override
        public ReadStream<T> resume() {
            pause = false;
            emit();
            return this;
        }

        private void emit() {
            while (elements.size() > 0 && !pause) {
                dataHandler.handle(elements.removeFirst());
            }
            if (!pause && elements.size() == 0 & endPending) {
                endHandler.handle(null);
            }
        }


        public boolean isPaused() {
            return pause;
        }

        public void send(T element) {
            elements.add(element);
            emit();
        }

        public void end() {
            if (elements.size() == 0 && !pause && endHandler != null) {
                endHandler.handle(null);
            } else {
                endPending = true;
            }
        }

        public void fail(Throwable t) {
            exceptionHandler.handle(t);
            endPending = false;
            pause = true;
            elements.clear();
        }
    }
}
