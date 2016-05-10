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

import io.github.bckfnn.callback.Callback;
import io.github.bckfnn.callback.CallbackReadStream;
import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.msg.Delete;
import io.github.bckfnn.mongodb.msg.GetMore;
import io.github.bckfnn.mongodb.msg.Insert;
import io.github.bckfnn.mongodb.msg.Query;
import io.github.bckfnn.mongodb.msg.Reply;
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

            database.execute(insert, concern, (result, error) -> {
                if (error != null) {
                    results.fail(error);
                    cnt.set(-1);
                } else {
                    results.send(new WriteResult(result.getDocuments().get(0)));
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

    public void insert(BsonDoc document, WriteConcern concern, final Callback<WriteResult> callback) {
        List<BsonDoc> part = new ArrayList<>();
        part.add(document);

        Insert insert = new Insert();
        insert.setCollectionName(fullname());
        insert.setMultiUpdate(false);

        insert.setDocuments(part);

        database.execute(insert, concern, WriteResult.result(callback));
    }

    public void update(BsonDoc q, BsonDoc o, boolean upsert, boolean multi, WriteConcern concern, Callback<WriteResult> callback) {
    }

    public void remove(BsonDoc o, WriteConcern concern, final Callback<WriteResult> callback) {
        Delete delete = new Delete();
        delete.setCollectionName(fullname());
        delete.setSelector(o);

        database.execute(delete, concern, WriteResult.result(callback));
    }

    public void findOne(String obj, BsonDoc fields, final Callback<BsonDoc> callback) {
        BsonDoc queryDoc = BsonDoc.newDoc(d -> d.put("_id", obj));
        findOne(queryDoc, fields, callback);
    }

    public void findOne(BsonDoc queryDoc, BsonDoc fields, Callback<BsonDoc> callback) {
        __find(queryDoc, fields, 0, -1, callback.one(result -> {
            callback.ok(result);
        }));
    }

    public void indexInformation(CallbackReadStream<BsonDoc> callback) {
        BsonDoc query = BsonDoc.newDoc(d -> d.put("ns", fullname()));
        BsonDoc fields = BsonDoc.newDoc(d -> d.put("ns", 0));

        database.collection("system.indexes").__find(query, fields, 0, 100, callback);
    }

    public void createIndex(BsonDoc keys, BsonDoc options, Callback<Void> callback) {

    }

    public void dropIndexes(String name, Callback<BsonDoc> callback) {
        dropIndex("*", callback);
    }

    public void dropIndex(String name, Callback<BsonDoc> callback) {
        BsonDoc cmd = BsonDoc.newDoc(d -> d.put("dropIndexes", collectionName).put("index", name));
        database.command(cmd, 0, 1, callback);
    }

    public void count(final Callback<Long> callback) {

        BsonDoc queryDoc = BsonDoc.newDoc(d -> d.put("count", collectionName));

        database.command(queryDoc, 0, 1, callback.call(result -> {
            callback.ok((long) result.getInt("n"));
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


    
    public void __find(BsonDoc ref, BsonDoc fields, int skip, int size, CallbackReadStream<BsonDoc> stream) {
        Query queryCmd = makeQuery(ref, fields, skip, size);

        stream.call(rs -> {
            database.execute(queryCmd, null, new Callback<Reply>() {
                @Override
                public void call(Reply result, Throwable error) {
                    if (error != null) {
                        rs.fail(error);
                    }
                    for (int i = 0; i < result.getNumberReturned(); i++) {
                        rs.send(result.getDocuments().get(i));
                    }
                    if (result.getCursorID() == 0) {
                        rs.end();
                    } else {
                        GetMore getMore =  new GetMore();
                        getMore.setCursorId(result.getCursorID());
                        getMore.setNumberToReturn(size);
                        getMore.setCollectionName(fullname());
                        database.execute(getMore, null, this);
                    }
                }
            });
        });
    }
}
