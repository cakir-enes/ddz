package dds.service.pubsub;

import com.google.gson.Gson;
import dds.service.Serde;
import dds.service.pubsub.nats.NatsPubSub;
import io.nats.client.Connection;
import io.nats.client.Nats;

import java.io.IOException;

public class PubSubFactory {

    public static <R> NatsPubSub<R> createFor(Class<R> clazz, String... tags) {
        try {
            Connection connection = Nats.connect();
            StringBuilder topicName = new StringBuilder(clazz.getName());
            for (String tag : tags) {
                topicName.append("-").append(tag);
            }
            System.err.println("Creating connection for" + topicName);
            return new NatsPubSub<R>(connection, jsonSerde(clazz), topicName.toString());
        } catch (IOException | InterruptedException e) {
            System.out.println("SHIT");
            e.printStackTrace();
        }
        return null;
    }

    private static <R> Serde<R> jsonSerde(Class<R> clazz) {
        Serde<R> serde = new Serde<R>() {
            private Gson gson = new Gson();
            @Override
            public byte[] serialize(R obj) {
                return gson.toJson(obj).getBytes();
            }

            @Override
            public R deserialize(byte[] obj) {
                return gson.fromJson(new String(obj), clazz);
            }
        };
        return serde;
    }
}
