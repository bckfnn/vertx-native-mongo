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

import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.bson.BsonDocMap;

public enum WriteConcern {
    NONE(-1, 0, false, false),
    ERRORS_IGNORED(-1, 0, false, false),
    UNACKNOWLEDGED(0, 0, false, false),
    NORMAL(0, 0, false, false),
    ACKNOWLEDGED(1, 0, false, false),
    SAFE(1, 0, false, false),
    REPLICAS_SAFE(2, 0, false, false),
    REPLICA_ACKNOWLEDGED(2, 0, false, false),
    FSYNCED(1, 0, true, false),
    JOURNALED(1, 0, false, true),
    MAJORITY(Integer.MAX_VALUE, 0, false, false),
    FSYNC_SAFE(1, 0, true, false),
    JOURNAL_SAFE(1, 0, false, true);

    int _w = 0;
    int _wtimeout = 0;
    boolean _fsync = false;
    boolean _j = false;
    boolean _continueOnErrorForInsert = false;

    private WriteConcern(int w) {
        this(w, 0, false, false);
    }

    private WriteConcern(int w, int wtimeout, boolean fsync, boolean j) {
        this(w, wtimeout, fsync, j, false);
    }

    private WriteConcern(int w, int wtimeout, boolean fsync, boolean j, boolean continueOnInsertError) {
        _w = w;
        _wtimeout = wtimeout;
        _fsync = fsync;
        _j = j;
        _continueOnErrorForInsert = continueOnInsertError;
    }

    public boolean callGetLastError() {
        return _w > 0;
    }

    public BsonDoc getCommand() {
        BsonDoc command = new BsonDocMap();
        command.putInt("getlasterror", 1);

        if (_w > 1) {
            command.putInt("w", _w);
        }

        if (_wtimeout > 0) {
            command.putInt("wtimeout", _wtimeout);
        }

        if (_fsync) {
            command.putBoolean("fsync", true);
        }

        if (_j) {
            command.putBoolean("j", true);
        }

        return command;
    }

}
