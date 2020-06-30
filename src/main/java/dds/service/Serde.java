package dds.service;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.function.Supplier;

public interface Serde<T> {

    byte[] serialize(T obj);

    T deserialize(byte[] obj);


    enum SerdeOptions {
        ;
        public static<R> Serde<R> kryo(Class<R> rClass, Class<?>... toRegister) {
            return new Serde<R>() {
                private final ThreadLocal<Kryo> kryo = ThreadLocal.withInitial(() -> {
                    System.err.println("CREATING KRYOO");
                    Kryo kryo = new Kryo();
                    kryo.register(rClass);
                    for (Class<?> aClass : toRegister) {
                        kryo.register(aClass);
                    }
                    return kryo;
                });
                @Override
                public byte[] serialize(R obj) {
                    Output out = new Output(new ByteArrayOutputStream());
                    kryo.get().writeObject(out, obj);
                    out.close();
                    return out.getBuffer();
                }

                @Override
                public R deserialize(byte[] obj) {
                    Input input = new Input(obj);
                    input.setBuffer(obj);
                    return kryo.get().readObject(input, rClass);
                }
            };
        }

        public static <R> Serde<R> json(Class<R> clazz) {
            return new Serde<R>() {
                private final Gson gson = new Gson();
                @Override
                public byte[] serialize(R obj) {
                    return gson.toJson(obj).getBytes();
                }

                @Override
                public R deserialize(byte[] obj) {
                    return gson.fromJson(new String(obj), clazz);
                }
            };
        }
    }
}
