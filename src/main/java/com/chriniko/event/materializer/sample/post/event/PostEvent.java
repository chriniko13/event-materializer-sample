package com.chriniko.event.materializer.sample.post.event;

import com.chriniko.event.materializer.sample.deserializer.InstantDeserializer;
import com.chriniko.event.materializer.sample.serializer.InstantSerializer;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "evt-type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PostCreatedEvent.class, name = "create"),
        @JsonSubTypes.Type(value = PostUpdatedEvent.class, name = "update"),
        @JsonSubTypes.Type(value = PostDeletedEvent.class, name = "delete"),
})

@NoArgsConstructor
@Getter
@Setter
@ToString
public abstract class PostEvent {

    protected String postId;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = InstantDeserializer.class)
    protected Instant creationDate;

}
