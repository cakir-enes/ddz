package dds.service;

public interface Serde<T> {

    byte[] serialize(T obj);

    T deserialize(byte[] obj);
}
