package com.github.bckfnn.mongodb;

import java.util.ArrayList;
import java.util.List;

import com.github.bckfnn.mongodb.bson.BsonBuilder;
import com.github.bckfnn.mongodb.bson.BsonDoc;
import com.github.bckfnn.mongodb.msg.Delete;
import com.github.bckfnn.mongodb.msg.GetMore;
import com.github.bckfnn.mongodb.msg.Insert;
import com.github.bckfnn.mongodb.msg.Query;
import com.github.bckfnn.mongodb.msg.Reply;


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
    
    public void insert(List<BsonDoc> documents, WriteConcern concern, final Callback<WriteResult> callback) {
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

            database.execute(insert, concern, WriteResult.result(callback));
        }
    }

    public void update(BsonDoc q, BsonDoc o, boolean upsert, boolean multi, WriteConcern concern, Callback<WriteResult> callback) {
    }

    public void remove(BsonDoc o, WriteConcern concern, final Callback<WriteResult> callback) {
        Delete delete = new Delete();
        delete.setCollectionName(fullname());
        delete.setSelector(o);

        database.execute(delete, concern, WriteResult.result(callback));
    }

    public void findOne(String obj, BsonDoc fields, final Callback<BsonDoc> handler) {
        BsonDoc queryDoc = BsonBuilder.doc().
                put("_id", obj).get();
        findOne(queryDoc, fields, handler);
    }
    
    public void findOne(BsonDoc queryDoc, BsonDoc fields, final Callback<BsonDoc> handler) {
        __find(queryDoc, fields, 0, -1, new Callback.Default<BsonDoc>(handler) {
            int cnt = 0;
            @Override
            public void handle(BsonDoc result) {
                if (cnt++ == 0) {
                    handler.handle(result);
                }
            }
        });
    }
    public void indexInformation(Callback<BsonDoc> callback) {
        BsonDoc query = BsonBuilder.doc().
                put("ns", fullname()).get();

        BsonDoc fields = BsonBuilder.doc().put("ns", 0).get();
        
        database.collection("system.indexes").__find(query, fields, 0, 100, callback);
    }

    public void createIndex(BsonDoc keys, BsonDoc options, Callback<Void> callback) {

    }

    public void dropIndexes(String name, Callback<BsonDoc> callback) {
        dropIndex("*", callback);
    }

    public void dropIndex(String name, Callback<BsonDoc> callback) {
        BsonDoc cmd = BsonBuilder.doc().
                put("dropIndexes", collectionName).
                put("index", name).get();
        database.command(cmd, 0, 1, callback);
    }

    public void count(final Callback<Long> handler) {

        BsonDoc queryDoc = BsonBuilder.doc().
                put("count", collectionName).
                get();

        database.command(queryDoc, 0, 1, new Callback.Default<BsonDoc>(handler) {
            @Override
            public void handle(BsonDoc result) {
                handler.handle((long) result.getDouble("n"));
            }
        });
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

    public void __find(BsonDoc ref, BsonDoc fields, int skip, final int size, final Callback<BsonDoc> handler) {
        Query queryCmd = makeQuery(ref, fields, skip, size);

        database.execute(queryCmd, null, new Callback.Default<Reply>(handler) {
            @Override
            public void handle(Reply reply) {
                //System.out.println(reply.getCursorID());
            	for (int i = 0; i < reply.getNumberReturned(); i++) {
            		handler.handle(reply.getDocuments().get(i));
            	}
            	if (reply.getCursorID() == 0) {
            	    handler.end();
            	} else {
            	    GetMore getMore =  new GetMore();
            	    getMore.setCursorId(reply.getCursorID());
            	    getMore.setNumberToReturn(size);
            	    getMore.setCollectionName(fullname());
            	    database.execute(getMore, null, this);
            	}
            }

            @Override
            public void end() {
            }
            
        });
    }
}
