package kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import java.util.Properties;

public class SimpleProducer {
    public static void main(String[] args) {
        // Set up properties
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // Create producer
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 1; i <= 5; i++) {
                String value = "Message " + i;
                ProducerRecord<String, String> record = new ProducerRecord<>("test-topic", value);
                RecordMetadata metadata = producer.send(record).get();
                System.out.printf("Sent: %s (partition %d, offset %d)%n", value, metadata.partition(), metadata.offset());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
