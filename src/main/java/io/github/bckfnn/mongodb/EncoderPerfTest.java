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
package io.github.bckfnn.mongodb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
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
import io.github.bckfnn.mongodb.bson.BsonDecoder;
import io.github.bckfnn.mongodb.bson.BsonDoc;
import io.github.bckfnn.mongodb.bson.BsonDocMap;
import io.github.bckfnn.mongodb.bson.BsonDouble;
import io.github.bckfnn.mongodb.bson.BsonEncoder;
import io.github.bckfnn.mongodb.bson.BsonInt;
import io.vertx.core.buffer.Buffer;

@Fork(5)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class EncoderPerfTest {
    BsonDoc doc;
    BsonDocument doc1;
    byte[] encoded;

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

        BsonArray arr = new BsonArray();
        arr.add(new BsonInt(43));
        arr.add(new BsonDouble(43));
        doc.putArray("array", arr);

        BsonEncoder enc = new BsonEncoder();
        enc.visitDocument(doc);
        System.out.println(enc.asBuffer() + " " + DatatypeConverter.printHexBinary(enc.asBuffer().getBytes()));
        encoded = enc.asBuffer().getBytes();


        doc1 = new BsonDocument();
        doc1.append("string", new BsonString("String value"));
        doc1.append("int", new BsonInt32(42));
        doc1.append("long", new BsonInt64(42424242L));
        doc1.append("double", new org.bson.BsonDouble(42.42));
        doc1.append("booleanTrue", BsonBoolean.TRUE);
        doc1.append("booleanFalse", BsonBoolean.TRUE);

        BsonDocument sub1 = new BsonDocument();
        sub1.append("sub", new BsonString("sub value"));
        doc1.append("subdoc", sub1);

        org.bson.BsonArray arr1 = new org.bson.BsonArray();
        arr1.add(new BsonInt32(43));
        arr1.add(new org.bson.BsonDouble(43d));
        doc1.append("array", arr1);

        BasicOutputBuffer out = new BasicOutputBuffer(512);
        new BsonDocumentCodec().encode(new BsonBinaryWriter(out), doc1, EncoderContext.builder().build());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        out.pipe(baos);

        System.out.println(baos.toString("UTF-8") + " " + DatatypeConverter.printHexBinary(baos.toByteArray()));

    }

    @Benchmark
	public Buffer encodeBsonTest() throws Exception {
        BsonEncoder enc = new BsonEncoder();
        enc.visitDocument(doc);

        return enc.asBuffer();
	}

    @Benchmark
    public BsonDoc decodeBsonTest() throws Exception {
        return BsonDecoder.decode(Buffer.buffer(encoded));
    }


    @Benchmark
    public Object encodeMongoTest() throws Exception {
        BasicOutputBuffer out = new BasicOutputBuffer(512);
        new BsonDocumentCodec().encode(new BsonBinaryWriter(out), doc1, EncoderContext.builder().build());
        return out.getByteBuffers();
    }

    @Benchmark
    public Object decodeMongoTest() throws Exception {
        BsonBinaryReader reader = new BsonBinaryReader(ByteBuffer.wrap(encoded));
        return new BsonDocumentCodec().decode(reader, DecoderContext.builder().build());
    }
}
