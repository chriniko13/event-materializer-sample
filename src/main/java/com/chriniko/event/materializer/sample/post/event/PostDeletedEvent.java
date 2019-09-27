package com.chriniko.event.materializer.sample.post.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class PostDeletedEvent extends PostEvent {

    public static PostDeletedEvent of(String id) {
        PostDeletedEvent event = new PostDeletedEvent();
        event.setPostId(id);
        event.setCreationDate(Instant.now());
        return event;
    }
}
