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
public class PostCreatedEvent extends PostEvent {

    private String author;
    private String description;
    private String text;

    public static PostCreatedEvent of(String id,
                                      String author,
                                      String description,
                                      String text,
                                      Instant creationDate) {
        PostCreatedEvent event = new PostCreatedEvent();
        event.setPostId(id);
        event.setAuthor(author);
        event.setDescription(description);
        event.setText(text);
        event.setCreationDate(creationDate);
        return event;
    }
}
