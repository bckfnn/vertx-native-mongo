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
package io.github.bckfnn.mongodb.bson;

final public class BsonInt implements Element, BsonNumber {
    private int value;

    public BsonInt(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public Number getNumber() {
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
