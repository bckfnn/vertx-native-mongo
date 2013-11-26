package com.github.bckfnn.mongodb.bson;

public class BsonLong implements Element {
    final private long value;

    public BsonLong(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitLong(name, this);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (value ^ (value >>> 32));
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
		BsonLong other = (BsonLong) obj;
		if (value != other.value)
			return false;
		return true;
	}
}
