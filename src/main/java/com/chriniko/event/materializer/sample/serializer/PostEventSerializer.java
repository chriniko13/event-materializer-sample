package com.chriniko.event.materializer.sample.serializer;

import com.chriniko.event.materializer.sample.post.event.PostEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Log4j2
public class PostEventSerializer implements Serializer<PostEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> map, boolean b) {
    }

    @Override
    public byte[] serialize(String topic, PostEvent postEvent) {
        try {
            return objectMapper.writeValueAsBytes(postEvent);
        } catch (Exception error) {
            log.error("could not serialize user event", error);
            return null;
        }
    }

    @Override
    public void close() {
    }
}
