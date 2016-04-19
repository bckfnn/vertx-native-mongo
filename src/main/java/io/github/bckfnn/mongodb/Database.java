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

import io.github.bckfnn.mongodb.bson.BsonBuilder;
import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.msg.OutMessage;
import io.github.bckfnn.mongodb.msg.Query;
import io.github.bckfnn.mongodb.msg.Reply;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;


public class Database {
	Client client;
	String name;

	public Database(Client client, String name) {
		this.client = client;
		this.name = name;
	}

	public String name() {
		return name;
	}

	public Client getClient() {
	    return client;
	}

    public Collection collection(String collectionName) {
        return new Collection(this, collectionName);
    }

    public void collectionsInfo(Handler<ReadStream<BsonDoc>> handler) {
        BsonDoc queryDoc = BsonBuilder.doc().get();
    	collection("system.namespaces").__find(queryDoc, null, 0, 10, Utils.each(handler, (func, doc) -> {
    	    func.accept(doc);
    	}));
    }

    public void collectionNames(Handler<ReadStream<String>> handler) {
    	collectionsInfo(Utils.each(handler, (func, doc) -> {
		    String cname = doc.getString("name").substring(name.length() + 1);
		    if (cname.indexOf('.') < 0) {
		        func.accept(cname);
		    }
    	}));
    }

    public void dropDatabase(Handler<AsyncResult<BsonDoc>> handler) {
        command("dropDatabase", 0, 1, handler);
    }

    public void createCollection(String name, Handler<AsyncResult<BsonDoc>> handler) {
        BsonDoc cmd = BsonBuilder.doc().put("create", name).get();
        command(cmd, 0, 1, handler);
    }

    public void execute(OutMessage msg, WriteConcern concern, Handler<AsyncResult<Reply>> handler) {
        Query lastError = null;

        if (concern != null && concern.callGetLastError()) {
            lastError = collection("$cmd").makeQuery(concern.getCommand(), null, 0, -1);
        }

        client.write(msg, lastError, handler);
    }

    public void command(final BsonDoc cmd, final int skip, final int size, final Handler<AsyncResult<BsonDoc>> handler) {
        collection("$cmd").__find(cmd, null, skip, size, Utils.one(handler, result -> {
			if (result.getDouble("ok") == 1) {
				handler.handle(Future.succeededFuture(result));
			} else {
			    handler.handle(Future.failedFuture(new Error(result.getString("err"))));
			}
        }));
    }

    public void command(String cmd, final int skip, final int size, final Handler<AsyncResult<BsonDoc>> handler) {
        BsonDoc cmdDoc = BsonBuilder.doc().put(cmd, 1).get();
        command(cmdDoc, skip, size, handler);
    }

    /*
     * add_son_manipulator
     * add_user
     * authenticate
     * collection_names
     * create_collection
     * derefrence
     * drop_collection
     * eval
     * logout
     * profiling_info
     * profiling_level
     * remove_user
     * reset_error_history
     * set_profiling_level
     * validate_collection
     * name
     * read_preference
     * secondary_acceptable_latency_ms
     * tag_sets
     * write_concern
     */
}
