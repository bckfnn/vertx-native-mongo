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

import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

final public class BsonBinary implements Element {
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

    @Override
    public String toString() {
        return DatatypeConverter.printHexBinary(value);
    }


}
