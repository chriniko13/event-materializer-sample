package com.chriniko.event.materializer.sample.post.control;

import com.chriniko.event.materializer.sample.core.Buffer;
import com.chriniko.event.materializer.sample.post.domain.Post;
import com.chriniko.event.materializer.sample.post.event.PostCreatedEvent;
import com.chriniko.event.materializer.sample.post.event.PostDeletedEvent;
import com.chriniko.event.materializer.sample.post.event.PostEvent;
import com.chriniko.event.materializer.sample.post.event.PostUpdatedEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@Log4j2
@Component
public class PostEventCalculator {

    public void calculateState(PostEvent postEvent, Buffer<Post, String> buffer) {

        Match(postEvent).of(

                Case($(instanceOf(PostCreatedEvent.class)), postCreatedEvent -> run(() -> {

                    String postId = postCreatedEvent.getPostId();

                    Post justCreatedPost = buffer.getOrCreate(postId);

                    justCreatedPost.setDescription(postCreatedEvent.getDescription());
                    justCreatedPost.setAuthor(postCreatedEvent.getAuthor());
                    justCreatedPost.setText(postCreatedEvent.getText());
                    justCreatedPost.setCreatedAt(postCreatedEvent.getCreationDate().toString());
                    justCreatedPost.setUpdatedAt(null);
                    justCreatedPost.setDescription(postCreatedEvent.getDescription());

                })),

                Case($(instanceOf(PostDeletedEvent.class)), postDeletedEvent -> run(() -> {
                    buffer.delete(postDeletedEvent.getPostId());
                })),

                Case($(instanceOf(PostUpdatedEvent.class)), postUpdatedEvent -> run(() -> {

                    Post post = buffer.findById(postUpdatedEvent.getPostId());

                    post.setUpdatedAt(postUpdatedEvent.getCreationDate().toString());
                    post.setAuthor(postUpdatedEvent.getAuthor());
                    post.setDescription(postUpdatedEvent.getDescription());
                    post.setText(postUpdatedEvent.getText());

                })),

                Case($(), unknownPostEvent -> run(() -> {
                    /*
                        Note: in a production system, we could have a datastructure where we will save unknown user events
                              and expose this data structure via JMX for example, or something similar.
                              Another way could be to fail fast and throw an exception.
                              It depends on the use case.
                     */
                    log.warn("unknown post event received: {}", unknownPostEvent.getClass().getName());
                }))
        );
    }
}
