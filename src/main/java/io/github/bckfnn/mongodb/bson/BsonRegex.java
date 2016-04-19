package io.github.bckfnn.mongodb.bson;

public class BsonRegex implements Element {
    private String value;
    private String options;

    public BsonRegex(String value, String options) {
        this.value = value;
        this.options = options;
    }

    public String getValue() {
        return value;
    }

    public String getOptions() {
        return options;
    }

    @Override
    public void accept(String name, Visitor visitor) {
        visitor.visitRegex(name, this);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		BsonRegex other = (BsonRegex) obj;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
    
    
}
