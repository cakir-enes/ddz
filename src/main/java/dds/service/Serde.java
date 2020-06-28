package dds.service;

import com.google.gson.Gson;

public interface Serde<T> {

    byte[] serialize(T obj);

    T deserialize(byte[] obj);


    enum SerdeOptions {
        ;
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
