package io.github.bckfnn.mongodb.bson;

public class BsonMaxkey implements Element {

    public BsonMaxkey() {
    }

    public Object getValue() {
        return null;
    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitMaxkey(name, this);
    }

    @Override
    public String toString() {
        return "null";
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}
}
