package io.github.bckfnn.mongodb.bson;

import java.util.Date;

public class BsonDatetime implements Element {
    private long value;

    public BsonDatetime(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitDatetime(name, this);
    }

    @Override
    public String toString() {
        return new Date(value).toString();
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
		BsonDatetime other = (BsonDatetime) obj;
		if (value != other.value)
			return false;
		return true;
	}
    
    
}
