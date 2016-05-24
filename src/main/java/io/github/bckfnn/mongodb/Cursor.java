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

import java.util.LinkedList;

import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

public class Cursor<T> implements ReadStream<T> {
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
        //emit();
        return this;
    }

    @Override
    public ReadStream<T> resume() {
        pause = false;
        //emit();
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
    
    /*
     * item
 	 * add_options
 	 * batch_size
 	 * clone
 	 * close
 	 * count
 	 * distinct
 	 * each
 	 * explain
 	 * hint
 	 * limit
 	 * max_scan
 	 * next_object
 	 * remove_options
 	 * rewind
 	 * skip
 	 * sort
 	 * tail
 	 * to_list
 	 * where
 	 * alive
 	 * cursor_id
 	 * fetch_next
     */
}
