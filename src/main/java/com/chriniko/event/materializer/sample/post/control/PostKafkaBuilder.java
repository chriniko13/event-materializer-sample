package com.chriniko.event.materializer.sample.post.control;

import com.chriniko.event.materializer.sample.core.Buffer;
import com.chriniko.event.materializer.sample.core.Builder;
import com.chriniko.event.materializer.sample.deserializer.PostEventDeserializer;
import com.chriniko.event.materializer.sample.post.domain.Post;
import com.chriniko.event.materializer.sample.post.event.PostEvent;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

@Log4j2

@Component
@Scope("prototype")
public class PostKafkaBuilder extends Builder<Post, String, String> {

    private final Consumer<String, PostEvent> consumer;
    private final PostEventCalculator postEventCalculator;

    @Autowired
    public PostKafkaBuilder(Buffer<Post, String> buffer,
                            PostEventCalculator postEventCalculator) {
        super(buffer);
        this.postEventCalculator = postEventCalculator;
        this.consumer = createConsumer(this.offset);
    }

    @Override
    protected String getInitialOffset() {
        return "groupId---" + UUID.randomUUID().toString();
    }

    @Override
    protected String synchronizeLogic() {

        ConsumerRecords<String, PostEvent> consumerRecords = consumer.poll(Duration.ofMillis(1000));

        if (!consumerRecords.isEmpty()) {

            consumerRecords.forEach(consumerRecord -> {

                PostEvent postEvent = consumerRecord.value();
                postEventCalculator.calculateState(postEvent, buffer);

                String recordInfoMsg = "Record key: " + consumerRecord.key()
                        + ", Record value: " + consumerRecord.value()
                        + ", Record partition: " + consumerRecord.partition()
                        + ", Record offset: " + consumerRecord.offset();
                log.info(recordInfoMsg);
            });

            consumer.commitAsync((offsets, exception) -> {
                if (exception != null) {
                    log.error("could not commit offset, error: " + exception.getMessage());
                }
            });
        }

        return this.offset;
    }

    private Consumer<String, PostEvent> createConsumer(String groupId) {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, PostEventDeserializer.class.getName());

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Consumer<String, PostEvent> c = new KafkaConsumer<>(props);
        c.subscribe(Collections.singletonList("posts"));

        return c;
    }

}
