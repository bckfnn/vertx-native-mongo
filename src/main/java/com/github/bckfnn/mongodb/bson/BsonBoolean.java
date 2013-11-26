package com.github.bckfnn.mongodb.bson;

public class BsonBoolean implements Element {
    private boolean value;

    public BsonBoolean(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitBoolean(name, this);
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (value ? 1231 : 1237);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BsonBoolean other = (BsonBoolean) obj;
        if (value != other.value)
            return false;
        return true;
    }
}
