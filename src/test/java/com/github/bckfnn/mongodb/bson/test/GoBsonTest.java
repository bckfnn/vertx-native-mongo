package com.github.bckfnn.mongodb.bson.test;



import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.regex.Pattern;

import org.junit.Test;

import com.github.bckfnn.mongodb.bson.BsonBuilder;
import com.github.bckfnn.mongodb.bson.BsonDecoder;
import com.github.bckfnn.mongodb.bson.BsonDoc;
import com.github.bckfnn.mongodb.bson.BsonEncoder;

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
        BsonDoc empty = BsonBuilder.doc().get();

        compare(empty, EMPTY_BSON);
    }

    @Test
    public void testFloat() {
        BsonDoc json = BsonBuilder.doc().put("_", 5.05).get();

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
        BsonDoc json = BsonBuilder.doc().put("_", "yo").get();

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
        BsonDoc json = BsonBuilder.doc().putDoc("_").put("a", true).get();
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
    	BsonDoc json = BsonBuilder.doc().put("_", true).get();

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
    	BsonDoc json = BsonBuilder.doc().put("_", false).get();

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
    	BsonDoc json = BsonBuilder.doc().putNull("_").get();

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
    	BsonDoc json = BsonBuilder.doc().put("_", Pattern.compile("ab")).get();

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
    	BsonDoc json = BsonBuilder.doc().put("_", new Date(258)).get();

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
    	BsonDoc json = BsonBuilder.doc().put("_", new byte[]{'y', 'o'}).get();

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
    	BsonDoc json = BsonBuilder.doc().put("_", 258).get();

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
    	BsonDoc json = BsonBuilder.doc().put("_", 258l).get();

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
    	BsonDoc json = BsonBuilder.doc().put("_", 258l << 32).get();

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
    	BsonDoc json = BsonBuilder.doc().putMinKey("_").get();

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
    	BsonDoc json = BsonBuilder.doc().putMaxKey("_").get();

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