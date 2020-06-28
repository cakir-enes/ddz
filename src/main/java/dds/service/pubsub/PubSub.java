package dds.service.pubsub;

import java.util.concurrent.TransferQueue;

public interface PubSub {

    void publish(String topicName, byte[] topic);

    TransferQueue<byte[]> subscribe(String topicName);

    void unsubscribe(String topicName);
}