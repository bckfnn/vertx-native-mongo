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

final public class BsonDocLazy extends BsonDocMap {
    private BsonDecoder decoder;
    private int pos;

    public BsonDocLazy() {
    }

    @Override
    protected void init() {
        if (decoder == null) {
            return;
        }

        BsonDecoder dec = decoder;
        decoder = null;
        dec.setPos(pos);
        dec.loadDocument(this);
    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitDocument(name, this);
    }

    @Override
    public void decode(BsonDecoder dec) {
        this.decoder = dec;
        this.pos = dec.getPos();
        int size = dec.int32() - 4;
        dec.setPos(dec.getPos() + size);
    }
}
