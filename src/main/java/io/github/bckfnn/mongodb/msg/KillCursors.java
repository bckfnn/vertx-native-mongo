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
