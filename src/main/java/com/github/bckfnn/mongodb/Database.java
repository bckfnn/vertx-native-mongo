package com.github.bckfnn.mongodb;

import com.github.bckfnn.mongodb.bson.BsonBuilder;
import com.github.bckfnn.mongodb.bson.BsonDoc;
import com.github.bckfnn.mongodb.msg.OutMessage;
import com.github.bckfnn.mongodb.msg.Query;
import com.github.bckfnn.mongodb.msg.Reply;


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
    
    public void collectionsInfo(Callback<BsonDoc> handler) {
        BsonDoc queryDoc = BsonBuilder.doc().get();
    	collection("system.namespaces").__find(queryDoc, null, 0, 10, handler);
    }

    public void collectionNames(final Callback<String> handler) {
    	collectionsInfo(new Callback<BsonDoc>() {
			@Override
			public void handle(BsonDoc result) {
			    String cname = result.getString("name").substring(name.length() + 1);
			    if (cname.indexOf('.') < 0) {
			        handler.handle(cname);
			    }
			}

			@Override
			public void error(Throwable error) {
				handler.error(error);
			}

			@Override
			public void end() {
				handler.end();
			}
    	});
    }
    
    public void dropDatabase(final Callback<BsonDoc> callback) {
        command("dropDatabase", 0, 1, callback);
    }

    public void createCollection(String name, Callback<BsonDoc> callback) {
        BsonDoc cmd = BsonBuilder.doc().put("create", name).get();
        command(cmd, 0, 1, callback);
    }
	
    public void execute(OutMessage msg, WriteConcern concern, final Callback<Reply> handler) {
        Query lastError = null;

        if (concern != null && concern.callGetLastError()) {
            lastError = collection("$cmd").makeQuery(concern.getCommand(), null, 0, -1);
        }

        client.write(msg, lastError, handler);
    }

    public void command(final BsonDoc cmd, final int skip, final int size, final Callback<BsonDoc> handler) {
        collection("$cmd").__find(cmd, null, skip, size, new Callback.Default<BsonDoc>(handler) {
			@Override
			public void handle(BsonDoc result) {
				if (result.getDouble("ok") == 1) {
					handler.handle(result);
				} else {
				    handler.error(new Error(result.getString("err")));
				}
			}
        });
    }

    public void command(String cmd, final int skip, final int size, final Callback<BsonDoc> handler) {
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
