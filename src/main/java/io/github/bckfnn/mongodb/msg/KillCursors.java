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
package io.github.bckfnn.mongodb.msg;

import java.util.List;

import io.github.bckfnn.mongodb.bson.BsonEncoder;


public class KillCursors extends OutMessage {
    private int flags;
    private List<Long> cursorIds;

    public void setSingleRemove(boolean singleRemove) {
        flags = flag(0, flags, singleRemove);
    }


    /**
     * @param cursorIds the cursorIds to set
     */
    public void setCursorIds(List<Long> cursorIds) {
        this.cursorIds = cursorIds;
    }



    @Override
    protected void doSend(BsonEncoder enc) {
        enc.int32(0); // ZERO
        enc.int32(cursorIds.size());
        for (long cursorId : cursorIds) {
            enc.int64(cursorId);
        }
    }

    @Override
    protected int getOpcode() {
        return OP_KILL_CURSORS;
    }
}
