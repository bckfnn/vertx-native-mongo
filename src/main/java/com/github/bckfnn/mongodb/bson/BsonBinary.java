package com.github.bckfnn.mongodb.bson;

import java.util.Arrays;

public class BsonBinary implements Element {
    private byte subtype;
    private byte[] value;

    public BsonBinary(byte subtype, byte[] value) {
        this.subtype = subtype;
        this.value = value;
    }

    public byte getSubtype() {
        return subtype;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitBinary(name, this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + subtype;
		result = prime * result + Arrays.hashCode(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BsonBinary other = (BsonBinary) obj;
		if (subtype != other.subtype)
			return false;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}
    
    
}
