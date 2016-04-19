package io.github.bckfnn.mongodb;

import java.util.ArrayList;
import java.util.List;

import io.github.bckfnn.mongodb.bson.BsonBuilder;
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

    public void insert(List<BsonDoc> documents, WriteConcern concern, final Handler<AsyncResult<WriteResult>> handler) {
        //WriteResult last = null;

        int cur = 0;
        int maxsize = database.client.getMaxBsonSize();
        while (cur < documents.size()) {

            Insert insert = new Insert();
            insert.setCollectionName(fullname());
            insert.setMultiUpdate(concern._continueOnErrorForInsert);

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
            insert.setDocuments(part);

            database.execute(insert, concern, WriteResult.result(handler));
        }
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
        BsonDoc queryDoc = BsonBuilder.doc().
                put("_id", obj).get();
        findOne(queryDoc, fields, handler);
    }

    public void findOne(BsonDoc queryDoc, BsonDoc fields, final Handler<AsyncResult<BsonDoc>> handler) {
        __find(queryDoc, fields, 0, -1, Utils.one(handler, result -> {
            handler.handle(Future.succeededFuture(result));
        }));
    }

    public void indexInformation(Handler<ReadStream<BsonDoc>> handler) {
        BsonDoc query = BsonBuilder.doc().
                put("ns", fullname()).get();

        BsonDoc fields = BsonBuilder.doc().put("ns", 0).get();

        database.collection("system.indexes").__find(query, fields, 0, 100, handler);
    }

    public void createIndex(BsonDoc keys, BsonDoc options, Handler<AsyncResult<Void>> handler) {

    }

    public void dropIndexes(String name, Handler<AsyncResult<BsonDoc>> handler) {
        dropIndex("*", handler);
    }

    public void dropIndex(String name, Handler<AsyncResult<BsonDoc>> handler) {
        BsonDoc cmd = BsonBuilder.doc().
                put("dropIndexes", collectionName).
                put("index", name).get();
        database.command(cmd, 0, 1, handler);
    }

    public void count(final Handler<AsyncResult<Long>> handler) {

        BsonDoc queryDoc = BsonBuilder.doc().
                put("count", collectionName).
                get();

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

    public void __find(BsonDoc ref, BsonDoc fields, int skip, int size, Handler<ReadStream<BsonDoc>> stream) {
        Query queryCmd = makeQuery(ref, fields, skip, size);

        Utils.ElementReadStream<BsonDoc> rs = new Utils.ElementReadStream<>();
        stream.handle(rs);
        database.execute(queryCmd, null, new Handler<AsyncResult<Reply>>() {
            @Override
            public void handle(AsyncResult<Reply> result) {
                if (result.failed()) {
                    //rs.fail(result.cause());
                }
                //System.out.println(reply.getCursorID());
                Reply reply = result.result();
            	for (int i = 0; i < reply.getNumberReturned(); i++) {
            		rs.send(reply.getDocuments().get(i));
            	}
            	if (reply.getCursorID() == 0) {
            	    rs.end();
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