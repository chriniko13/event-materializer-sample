package com.chriniko.event.materializer.sample.post.boundary;

import com.chriniko.event.materializer.sample.error.ProcessingException;
import com.chriniko.event.materializer.sample.post.domain.Post;
import com.chriniko.event.materializer.sample.post.event.PostCreatedEvent;
import com.chriniko.event.materializer.sample.post.event.PostDeletedEvent;
import com.chriniko.event.materializer.sample.post.event.PostEvent;
import com.chriniko.event.materializer.sample.post.event.PostUpdatedEvent;
import com.chriniko.event.materializer.sample.serializer.PostEventSerializer;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Log4j2

@Component
public class PostEventsKafkaInjector {

    private static final String POSTS_TOPIC_NAME = "posts";

    private static final String KAFKA_CONTACT_POINT
            = Optional.ofNullable(System.getenv("KAFKA_CONTACT_POINT")).orElse("localhost:9092");

    private KafkaProducer<String, PostEvent> kafkaProducer;

    @PostConstruct
    void init() {

        final Properties properties = new Properties();

        setupBootstrapAndSerializers(properties);
        setupBatchingAndCompression(properties);
        setupRetriesInFlightTimeout(properties);

        // set number of acknowledgments - acks - default is all
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        // send blocks up to 3 seconds
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000);

        kafkaProducer = new KafkaProducer<>(properties);
    }


    public void createPost(Post post) {

        PostCreatedEvent postCreatedEvent = PostCreatedEvent.of(post.getId(),
                post.getAuthor(),
                post.getDescription(),
                post.getText(),
                Instant.now()
        );

        final ProducerRecord<String, PostEvent> record
                = new ProducerRecord<>(POSTS_TOPIC_NAME, post.getId(), postCreatedEvent);

        try {
            kafkaProducer.send(record, (recordMetadata, error) -> {
                if (error != null) {
                    log.error("post event send operation failed", error);
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("createPost operation failed", e);
            throw new ProcessingException(e);
        }

    }

    public void deletePost(Post post) {

        PostDeletedEvent postDeletedEvent = PostDeletedEvent.of(post.getId());

        final ProducerRecord<String, PostEvent> record
                = new ProducerRecord<>(POSTS_TOPIC_NAME, post.getId(), postDeletedEvent);

        try {
            kafkaProducer.send(record, (recordMetadata, error) -> {
                if (error != null) {
                    log.error("post event send operation failed", error);
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("deletePost operation failed", e);
            throw new ProcessingException(e);
        }

    }

    public void updatePost(Post post) {

        PostUpdatedEvent postUpdatedEvent = PostUpdatedEvent.of(post.getId(),
                post.getAuthor(),
                post.getDescription(),
                post.getText());

        final ProducerRecord<String, PostEvent> record
                = new ProducerRecord<>(POSTS_TOPIC_NAME, post.getId(), postUpdatedEvent);

        try {
            kafkaProducer.send(record, (recordMetadata, error) -> {
                if (error != null) {
                    log.error("post event send operation failed", error);
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("updatePost operation failed", e);
            throw new ProcessingException(e);
        }

    }

    private void setupBootstrapAndSerializers(Properties properties) {
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTACT_POINT);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "PostEventsKafkaProducer---" + UUID.randomUUID().toString());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, PostEventSerializer.class.getName());
    }

    private void setupBatchingAndCompression(Properties properties) {
        // holds up to 64mb default is 32mb for all partition buffers
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33_554_432 * 2);
        // batch per partition, holds up to 64k per partition, default is 16k
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16_384 * 4);
        // wait up to 500 ms to batch to Kafka
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 50);
        // set compression type to snappy
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
    }

    private void setupRetriesInFlightTimeout(Properties properties) {
        // only one in-flight message per kafka broker connection
        properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        // we get request timeout in 15 seconds, default is 30 seconds
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15_000);
        // set the number of retries
        properties.put(ProducerConfig.RETRIES_CONFIG, 5);
        // only retry after one second
        properties.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1_000);
    }
}
