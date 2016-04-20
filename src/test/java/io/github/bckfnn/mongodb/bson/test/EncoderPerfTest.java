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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import io.github.bckfnn.mongodb.bson.BsonArray;
import io.github.bckfnn.mongodb.bson.BsonArrayList;
import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.bson.BsonDocMap;
import io.github.bckfnn.mongodb.bson.BsonDouble;
import io.github.bckfnn.mongodb.bson.BsonEncoder;
import io.github.bckfnn.mongodb.bson.BsonInt;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

@Fork(5)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class EncoderPerfTest {
    BsonDoc doc;

    @Setup
    public void setup() throws IOException {
        final BsonDoc doc = new BsonDocMap();
        doc.putString("string", "String value");
        doc.putInt("int", 42);
        doc.putLong("long", 42424242L);
        doc.putDouble("double", 42.42);
        doc.putBoolean("booleanTrue", true);
        doc.putBoolean("booleanFalse", false);

        BsonDoc sub = new BsonDocMap();
        sub.putString("sub", "sub value");
        doc.putDocument("subdoc", sub);

        BsonArray arr = new BsonArrayList();
        arr.add(new BsonInt(43));
        arr.add(new BsonDouble(43));
        doc.putArray("array", arr);
    }

    @Benchmark
	public Buffer bsonTest() throws Exception {
        BsonEncoder enc = new BsonEncoder();
        enc.visitDocument(doc);
        return enc.asBuffer();
	}
/*
	@Test
	public void perfTest() throws Exception {

		final BasicDBObject doc1 = new BasicDBObject();
		doc1.put("string", "String value");
		doc1.put("int", 42);
		doc1.put("long", 42424242L);
		doc1.put("double", 42.42);
		doc1.put("booleanTrue", true);
		doc1.put("booleanFalse", false);

		BasicDBObject sub1 = new BasicDBObject();
		sub1.put("sub", "sub value");
		doc1.put("subdoc", sub1);

		List<Object> arr1 = new ArrayList<Object>();
		arr1.add(43);
		arr1.add(43d);
		doc1.put("array", arr1);


		final BasicBSONEncoder encoder = new BasicBSONEncoder();
		System.out.println(new String(encoder.encode(doc1)));

		test(new Runnable() {
			@Override
			public void run() {
				encoder.encode(doc1);
			}
		}, 2000000);
	}
*/


	//@Test
	public void copyTest() throws Exception {
		final JsonObject map = new JsonObject();
		map.put("key1", 1);
		map.put("key2", 1l);
		map.put("key3", "abc");

		test(new Runnable() {
			@Override
			public void run() {
				map.copy();
			}
		});
	}


	//@Test
	public void copyTestMap() throws Exception {
		final Map<String, Object> map = new HashMap<>();
		map.put("key1",  1l);
		map.put("key2",  1);
		map.put("key3",  "abc");
		map.put("key4",  1);
		map.put("key5",  1);
		map.put("key6",  1);
		map.put("key7",  1);
		map.put("key8",  1);
		map.put("key9",  1);
		map.put("key0",  1);
		map.put("key10",  1);

		test(new Runnable() {
			@Override
			public void run() {
				copy(map);
			}
		});
		test(new Runnable() {
			@Override
			public void run() {
				copy(map);
			}
		});
	}


	//@Test
	public void jsonTest() throws Exception {
		final Map<String, Object> map = new HashMap<>();
		map.put("key1",  "a");
		map.put("key2",  "b");
		map.put("key3",  "abc");

		test(new Runnable() {
			@Override
			public void run() {
				Output out = new Output();
				encode(map, out);
			}
		});
		test(new Runnable() {
			@Override
			public void run() {
				Output out = new Output();
				encode(map, out);
			}
		});
	}

	//@Test
	public void jsonObjectTest() throws Exception {
		final JsonObject map = new JsonObject();
		map.put("key1", 1);
		map.put("key2", 1l);
		map.put("key3", "abc");
		System.out.println(map.encode());
		test(new Runnable() {
			@Override
			public void run() {
				//ByteArrayOutputStream ostr = new ByteArrayOutputStream();
				map.encode();
			}
		});
		test(new Runnable() {
			@Override
			public void run() {
				//ByteArrayOutputStream ostr = new ByteArrayOutputStream();
				map.encode();
			}
		});
	}

	private void test(Runnable run) {
		test(run, 10000000);
	}

	private void test(Runnable run, int cnt) {
		for (int i = 0; i < cnt; i++) {
			run.run();
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long t = System.currentTimeMillis();

		for (int i = 0; i < cnt; i++) {
			run.run();
		}
		System.out.println(System.currentTimeMillis()  - t);
	}

	@SuppressWarnings("unchecked")
	private Object copy(Object val) {
		if (val == null) {
			return null;
		} else {

			Class<?> cls = val.getClass();
			if (cls == HashMap.class) {
				//return new HashMap<>(); //((HashMap) val).clone();
				return copyMap((Map<String, Object>) val);
			} else if (cls == Integer.class) {
				return val;
			} else if (cls == Long.class) {
				return val;
			} else if (cls == String.class) {
				return val;
			} else {
				throw new RuntimeException("unexpected value " + val);
			}
		}
	}

	private Object copyMap(Map<String, Object> map) {
		HashMap<String, Object> r = new HashMap<>();


		for (Map.Entry<String, Object> e : map.entrySet()) {
			r.put(e.getKey(), copy(e.getValue()));
		}


/*
		for (String e : map.keySet()) {
			r.put(e, copy(map.get(e)));
		}
*/
		return r;
	}

	@SuppressWarnings("unchecked")
	private void encode(Object val, Output out) {
		if (val == null) {

		} else {

			Class<?> cls = val.getClass();
			if (cls == HashMap.class) {
				encodeMap((Map<String, Object>) val, out);
			} else if (cls == Integer.class) {
				encodeNumber((Integer) val, out);
			} else if (cls == Long.class) {
				encodeNumber((Long) val, out);
			} else if (cls == String.class) {
				encodeString((String) val, out);
			}

			/*
			//Class<?> cls = val.getClass();
			if (val instanceof Map) {
				encodeMap((Map<String, Object>) val, out);
			} else if (val instanceof Integer) {
				encodeNumber((Integer) val, out);
			} else if (val instanceof Long) {
				encodeNumber((Long) val, out);
			} else if (val instanceof String) {
				encodeString((String) val, out);
			}
			*/
		}
	}

	private void encodeNumber(Number val, Output out) {
		out.append(val.toString());
	}

	private void encodeString(String val, Output out) {
		out.append('"');
		out.append(val);
		out.append('"');
	}

	private void encodeMap(Map<String, Object> map, Output out) {
		out.append('{');
		boolean first = true;
		for (Map.Entry<String, Object> e : map.entrySet()) {
			if (!first) {
				out.append(',');
			}
			first = false;
			out.append('"');
			out.append(e.getKey());
			out.append('"');
			out.append(':');
			encode(e.getValue(), out);
		}
		out.append('}');
	}

	static class Output {
		char[] buffer = new char[40];
		int pos = 0;

		public void append(char ch) {
			buffer[pos++] = ch;
		}
		public void append(String str) {
			int l = str.length();
			str.getChars(0, l, buffer, pos += l);
		}
	}
}
