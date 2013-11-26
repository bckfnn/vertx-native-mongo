package com.github.bckfnn.mongodb.test;

import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.testtools.VertxAssert;

import com.github.bckfnn.mongodb.Callback;
import com.github.bckfnn.mongodb.Client;
import com.github.bckfnn.mongodb.Database;


public class Utils {
    
    abstract static class Success<T> implements Callback<T> {
        Seq<?> seq;
        public Success(Seq<?> seq) {
            this.seq = seq;
        }

        @Override
        public void error(Throwable error) {
            VertxAssert.fail(error.toString());
        }

        @Override
        public void end() {
            seq.next();
        }
    }

    public static class ToList<T> implements Callback<T> {
        List<T> list = new ArrayList<T>();
        Callback<List<T>> next;
        
        ToList(Callback<List<T>> next) {
            this.next = next;
        }
        
        @Override
        public void handle(T result) {
            list.add(result);
        }

        @Override
        public void error(Throwable error) {
            next.error(error);
        }

        @Override
        public void end() {
            next.handle(list);
            next.end();
        }
    }
    
    public static Handler<Client> open(final Vertx vertx, final Utils.Seq<Client> seq) {
        return new Handler<Client>() {
            public void handle(Client event) {
                final Client client = new Client(vertx, "localhost", 27017);
                client.open(new Handler<AsyncResult<Client>>() {
                    @Override
                    public void handle(AsyncResult<Client> event) {
                        VertxAssert.assertTrue(event.succeeded());
                        seq.setValue(client);
                        seq.next();
                    }
                });

            }
        };
    }

    public static Handler<Database> open(final Vertx vertx, final Seq<Database> seq, final String database) {
        return new Handler<Database>() {
            public void handle(Database event) {
                final Client client = new Client(vertx, "localhost", 27017);
                client.open(new Handler<AsyncResult<Client>>() {
                    @Override
                    public void handle(AsyncResult<Client> event) {
                        VertxAssert.assertTrue(event.succeeded());
                        seq.setValue(client.database(database));
                        seq.next();
                    }
                });
            }
        };
    }

    public static class Seq<T> {
        T value;
        List<Handler<T>> list = new ArrayList<>();

        public Seq<T> add(Handler<T> handler) {
            list.add(handler);
            return this;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public void next() {
            list.remove(0).handle(value);
        }
    }
}
