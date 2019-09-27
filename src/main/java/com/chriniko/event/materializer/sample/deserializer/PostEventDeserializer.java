package com.chriniko.event.materializer.sample.deserializer;

import com.chriniko.event.materializer.sample.post.event.PostEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.serialization.Deserializer;

@Log4j2
public class PostEventDeserializer implements Deserializer<PostEvent> {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PostEvent deserialize(String topic, byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, PostEvent.class);
        } catch (Exception error) {
            log.error("could not deserialize user event", error);
            return null;
        }
    }
}
