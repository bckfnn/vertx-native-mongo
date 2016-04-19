package io.github.bckfnn.mongodb.bson;

public class BsonInt implements Element {
    private int value;

    public BsonInt(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitInt(name, this);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
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
		BsonInt other = (BsonInt) obj;
		if (value != other.value)
			return false;
		return true;
	}
}
