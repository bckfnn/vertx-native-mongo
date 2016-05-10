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

import io.github.bckfnn.callback.Callback;
import io.github.bckfnn.callback.CallbackReadStream;
import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.bson.Element;
import io.github.bckfnn.mongodb.msg.OutMessage;
import io.github.bckfnn.mongodb.msg.Query;
import io.github.bckfnn.mongodb.msg.Reply;


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

    public void collectionsInfo(CallbackReadStream<BsonDoc> callback) {
        BsonDoc queryDoc = BsonDoc.newDoc(d -> {});
        /*
    	collection("system.namespaces").__find(queryDoc, null, 0, 10, callback.each((func, doc) -> {
    	    func.accept(doc);
    	}));
    	*/
        callback.call(rs -> {
            command("listCollections", 0, -1, (res, err) -> {
                System.out.println(res + " " + err);
                for (Element x : res.getDocument("cursor").getArray("firstBatch")) {
                    rs.send((BsonDoc) x); 
                }
                rs.end();
            });
        });
    }

    public void collectionNames(CallbackReadStream<String> callback) {
    	collectionsInfo(callback.each((func, doc) -> {
		    String cname = doc.getString("name");
	        func.accept(cname);
    	}));
    }

    public void dropDatabase(Callback<BsonDoc> callback) {
        command("dropDatabase", 0, 1, callback);
    }

    public void createCollection(String name, Callback<BsonDoc> callback) {
        BsonDoc cmd = BsonDoc.newDoc(d -> d.put("create", name));
        command(cmd, 0, 1, callback);
    }

    public void execute(OutMessage msg, WriteConcern concern, Callback<Reply> callback) {
        Query lastError = null;

        if (concern != null && concern.callGetLastError()) {
            lastError = collection("$cmd").makeQuery(concern.getCommand(), null, 0, -1);
        }

        client.write(msg, lastError, callback);
    }

    public void command(BsonDoc cmd, int skip, int size, Callback<BsonDoc> callback) {
        collection("$cmd").__find(cmd, null, skip, size, callback.one(result -> {
			if (result.getDouble("ok") == 1) {
				callback.ok(result);
			} else {
			    callback.fail(new Error(result.getString("err")));
			}
        }));
    }

    public void command(String cmd, int skip, int size, Callback<BsonDoc> handler) {
        BsonDoc cmdDoc = BsonDoc.newDoc(d -> d.put(cmd, 1));
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
