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
package io.github.bckfnn.mongodb.bson.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.regex.Pattern;

import org.junit.Test;

import io.github.bckfnn.mongodb.bson.BsonDecoder;
import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.bson.BsonEncoder;
import io.vertx.core.buffer.Buffer;

/**
 * This class contains tests inspired from the gobson project.
 */
public class GoBsonTest {

    private static final byte[] EMPTY_BSON = new byte[]{
            5,
            0,
            0,
            0,
            0
    };

    @Test
    public void testEmptyMap() {
        BsonDoc empty = BsonDoc.newDoc(d -> {});

        compare(empty, EMPTY_BSON);
    }

    @Test
    public void testFloat() {
        BsonDoc json = BsonDoc.newDoc(d -> d.put("_", 5.05));

        byte[] expected = new byte[]{
                // length
                0x10, 0x0, 0x0, 0x0,
                0x01, '_', 0x00, '3', '3', '3', '3', '3', '3', 0x14, '@',
                // end
                0x00};

        compare(json, expected);
    }

    @Test
    public void testString() {
        BsonDoc json = BsonDoc.newDoc(d -> d.put("_", "yo"));

        byte[] expected = new byte[]{
                // length
                0x0f, 0x00, 0x00, 0x00,
                0x02, '_', 0x00, 0x03, 0x00, 0x00, 0x00, 'y', 'o', 0x00,
                // end
                0x00};

        compare(json, expected);
    }

    @Test
    public void testSubDocumentBoolean() {
        BsonDoc json = BsonDoc.newDoc(d -> {
            d.putDoc("_", d2 -> {
                d2.put("a", true);
            });
        });
        System.out.println(json);
        byte[] expected = new byte[]{
                // length
                0x11, 0x00, 0x00, 0x00,
                0x03, '_', 0x00, 0x09, 0x00, 0x00, 0x00, 0x08, 'a', 0x00, 0x01, 0x00,
                // end
                0x00
        };

        compare(json, expected);
    }

    @Test
    public void testTrue() {
    	BsonDoc json = BsonDoc.newDoc(d -> d.put("_", true));

        byte[] expected = new byte[]{
                // length
                0x09, 0x00, 0x00, 0x00,
                0x08, '_', 0x00, 0x01,
                // end
                0x00
        };

        compare(json, expected);
    }

    @Test
    public void testFalse() {
    	BsonDoc json = BsonDoc.newDoc(d -> d.put("_", false));

        byte[] expected = new byte[]{
                // length
                0x09, 0x00, 0x00, 0x00,
                0x08, '_', 0x00, 0x00,
                // end
                0x00
        };

        compare(json, expected);
    }

    @Test
    public void testNull() {
    	BsonDoc json = BsonDoc.newDoc(d -> d.putNull("_"));

        byte[] expected = new byte[]{
                // length
                0x08, 0x00, 0x00, 0x00,
                0x0a, '_', 0x00,
                // end
                0x00
        };

        compare(json, expected);
    }

    @Test
    public void testRegEx() {
    	BsonDoc json = BsonDoc.newDoc(d -> d.put("_", Pattern.compile("ab")));

        byte[] expected = new byte[]{
                // length
                0x0c, 0x00, 0x00, 0x00,
                0x0b, '_', 0x00, 'a', 'b', 0x00, 0x00,
                // end
                0x00
        };

        compare(json, expected);
    }

    @Test
    public void testDate() {
    	BsonDoc json = BsonDoc.newDoc(d -> d.put("_", new Date(258)));

        byte[] expected = new byte[]{
                // length
                0x10, 0x00, 0x00, 0x00,
                0x09, '_', 0x00, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // end
                0x00
        };

        compare(json, expected);
    }

    @Test
    public void testBinary() {
    	BsonDoc json = BsonDoc.newDoc(d -> d.put("_", new byte[]{'y', 'o'}));

        byte[] expected = new byte[]{
                // length
                0x0f, 0x00, 0x00, 0x00,
                0x05, '_', 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 'y', 'o',
                // end
                0x00
        };

        compare(json, expected);
    }

    @Test
    public void testInt32() {
    	BsonDoc json = BsonDoc.newDoc(d -> d.put("_", 258));

    	byte[] expected = new byte[]{
                // length
                0x0c, 0x00, 0x00, 0x00,
                0x10, '_', 0x00, 0x02, 0x01, 0x00, 0x00,
                // end
                0x00
        };

        compare(json, expected);
    }

    @Test
    public void testInt64() {
    	BsonDoc json = BsonDoc.newDoc(d -> d.put("_", 258l));

        byte[] expected = new byte[]{
                // length
                0x10, 0x00, 0x00, 0x00,
                0x12, '_', 0x00, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // end
                0x00
        };

        compare(json, expected);
    }

    @Test
    public void testInt64_2() {
    	BsonDoc json = BsonDoc.newDoc(d -> d.put("_", 258l << 32));

        byte[] expected = new byte[]{
                // length
                0x10, 0x00, 0x00, 0x00,
                0x12, '_', 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x01, 0x00, 0x00,
                // end
                0x00
        };

        compare(json, expected);
    }

    @Test
    public void testMinKey() {
    	BsonDoc json = BsonDoc.newDoc(d -> d.putMinkey("_"));

        byte[] expected = new byte[]{
                // length
                0x08, 0x00, 0x00, 0x00,
                (byte) 0xff, '_', 0x00,
                // end
                0x00
        };

        compare(json, expected);
    }

    @Test
    public void testMaxKey() {
    	BsonDoc json = BsonDoc.newDoc(d -> d.putMaxkey("_"));

        byte[] expected = new byte[]{
                // length
                0x08, 0x00, 0x00, 0x00,
                0x7f, '_', 0x00,
                // end
                0x00
        };

        compare(json, expected);
    }

    private void compare(BsonDoc doc, byte[] expected) {
        Buffer buffer = BsonEncoder.encode(doc);

        assertArrayEquals(expected, buffer.getBytes());

        // reverse
        BsonDoc document = BsonDecoder.decode(Buffer.buffer(expected));
        assertEquals(doc, document);
    }
}