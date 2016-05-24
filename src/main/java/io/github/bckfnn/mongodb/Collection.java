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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.msg.Delete;
import io.github.bckfnn.mongodb.msg.GetMore;
import io.github.bckfnn.mongodb.msg.Insert;
import io.github.bckfnn.mongodb.msg.Query;
import io.github.bckfnn.mongodb.msg.Reply;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;


/**
 * aggregate
 * create_index
 * distinct
 * ensure_index
 * find
 * find_and_modify
 * find_one
 * group
 * inline_map_reduce
 * insert
 * map_reduce
 * options
 * reindex
 * remove
 * rename
 * save
 * update
 * read_preference
 * secondary_acceptable_latency_ms
 * tag_sets
 * uuid_subtype
 * write_concern
 *
 * @author INN
 *
 */
public class Collection {

    private final Database database;
    private final String collectionName;

    public Collection(Database database, String collectionName) {
        this.database = database;
        this.collectionName = collectionName;
    }

    public String name() {
    	return collectionName;
    }

    public String fullname() {
    	return database.name() + "." + name();
    }

    public Database database() {
    	return database;
    }

    public void insert(List<BsonDoc> documents, WriteConcern concern, final Handler<ReadStream<WriteResult>> handler) {
        AtomicInteger cnt = new AtomicInteger(0);

        Utils.ElementReadStream<WriteResult> results = new Utils.ElementReadStream<WriteResult>();
        handler.handle(results);

        int cur = 0;
        int maxsize = database.client.getMaxBsonSize();
        while (cur < documents.size()) {
            List<BsonDoc> part = new ArrayList<>();
            for (; cur < documents.size(); cur++) {
                BsonDoc o = documents.get(cur);
                part.add(o);

                // limit for batch insert is 4 x maxbson on server, use 2 x to be safe
                if (part.size() > 2 * maxsize) {
                    cur++;
                    break;
                }
            }

            Insert insert = new Insert();
            insert.setCollectionName(fullname());
            insert.setMultiUpdate(concern._continueOnErrorForInsert);

            insert.setDocuments(part);
            cnt.addAndGet(1);

            database.execute(insert, concern, ar -> {
                if (ar.failed()) {
                    results.fail(ar.cause());
                    cnt.set(-1);
                } else {
                    results.send(new WriteResult(ar.result().getDocuments().get(0)));
                    if (cnt.decrementAndGet() == 0) {
                        results.end();
                    }
                }
            });
        }
        if (cnt.get() == 0) {
            results.end();
        }
    }

    public void insert(BsonDoc document, WriteConcern concern, final Handler<AsyncResult<WriteResult>> handler) {
        List<BsonDoc> part = new ArrayList<>();
        part.add(document);

        Insert insert = new Insert();
        insert.setCollectionName(fullname());
        insert.setMultiUpdate(false);

        insert.setDocuments(part);

        database.execute(insert, concern, WriteResult.result(handler));
    }

    public void update(BsonDoc q, BsonDoc o, boolean upsert, boolean multi, WriteConcern concern, Handler<AsyncResult<WriteResult>> handler) {
    }

    public void remove(BsonDoc o, WriteConcern concern, final Handler<AsyncResult<WriteResult>> handler) {
        Delete delete = new Delete();
        delete.setCollectionName(fullname());
        delete.setSelector(o);

        database.execute(delete, concern, WriteResult.result(handler));
    }

    public void findOne(String obj, BsonDoc fields, final Handler<AsyncResult<BsonDoc>> handler) {
        BsonDoc queryDoc = BsonDoc.newDoc(d -> d.put("_id", obj));
        findOne(queryDoc, fields, handler);
    }

    public void findOne(BsonDoc queryDoc, BsonDoc fields, final Handler<AsyncResult<BsonDoc>> handler) {
        __find(queryDoc, fields, 0, -1, Utils.one(handler, result -> {
            handler.handle(Future.succeededFuture(result));
        }));
    }

    public void indexInformation(Handler<AsyncResult<Cursor<BsonDoc>>> handler) {
        BsonDoc query = BsonDoc.newDoc(d -> d.put("ns", fullname()));
        BsonDoc fields = BsonDoc.newDoc(d -> d.put("ns", 0));

        database.collection("system.indexes").__find(query, fields, 0, 100, handler);
    }

    public void createIndex(BsonDoc keys, BsonDoc options, Handler<AsyncResult<Void>> handler) {

    }

    public void dropIndexes(String name, Handler<AsyncResult<BsonDoc>> handler) {
        dropIndex("*", handler);
    }

    public void dropIndex(String name, Handler<AsyncResult<BsonDoc>> handler) {
        BsonDoc cmd = BsonDoc.newDoc(d -> d.put("dropIndexes", collectionName).put("index", name));
        database.command(cmd, 0, 1, handler);
    }

    public void count(final Handler<AsyncResult<Long>> handler) {

        BsonDoc queryDoc = BsonDoc.newDoc(d -> d.put("count", collectionName));

        database.command(queryDoc, 0, 1, Utils.handler(handler, result -> {
            handler.handle(Future.succeededFuture((long) result.getInt("n")));
        }));
    }

    public Query makeQuery(BsonDoc ref, BsonDoc fields, int skip, int size) {
        Query queryCmd = new Query();
        queryCmd.setCollectionName(fullname());
        queryCmd.setNoCursorTimeout(true);
        queryCmd.setNumberToSkip(skip);
        queryCmd.setNumberToReturn(size);
        queryCmd.setSelector(ref);
        queryCmd.setReturnFieldSelector(fields);
        return queryCmd;
    }

    public void __find(BsonDoc ref, BsonDoc fields, int skip, int size, Handler<AsyncResult<Cursor<BsonDoc>>> handler) {
        Query queryCmd = makeQuery(ref, fields, skip, size);

        Cursor<BsonDoc> cursor = new Cursor<>();
        database.execute(queryCmd, null, new Handler<AsyncResult<Reply>>() {
            @Override
            public void handle(AsyncResult<Reply> result) {
                if (result.failed()) {
                    handler.handle(Future.failedFuture(result.cause()));
                    return;
                }
                Reply reply = result.result();
                if (reply.getStartingFrom() == 0) {
                    handler.handle(Future.succeededFuture(cursor));
                }
                //System.out.println(reply.getCursorID());
            	for (int i = 0; i < reply.getNumberReturned(); i++) {
            		cursor.send(reply.getDocuments().get(i));
            	}
            	if (reply.getCursorID() == 0) {
            	    cursor.end();
            	} else {
            	    GetMore getMore =  new GetMore();
            	    getMore.setCursorId(reply.getCursorID());
            	    getMore.setNumberToReturn(size);
            	    getMore.setCollectionName(fullname());
            	    database.execute(getMore, null, this);
            	}
            }
        });
    }
}
