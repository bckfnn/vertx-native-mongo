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
package io.github.bckfnn.mongodb.bson;

import io.vertx.core.buffer.Buffer;

public class BsonArrayLazy extends BsonArray {
    private Buffer buffer;

    public BsonArrayLazy(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    protected void init() {
        if (buffer == null) {
            return;
        }
        Buffer b = buffer;
        buffer = null;

        BsonDecoder dec = new BsonDecoder(b);
        dec.loadArray(this);
    }

}
