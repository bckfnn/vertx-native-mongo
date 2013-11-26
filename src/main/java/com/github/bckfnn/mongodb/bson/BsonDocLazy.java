package com.github.bckfnn.mongodb.bson;

public class BsonDocLazy extends BsonDocMap {
    private BsonDecoder decoder;
    private int pos;

    public BsonDocLazy(BsonDecoder decoder, int pos) {
        this.decoder = decoder;
        this.pos = pos;
    }

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
}
