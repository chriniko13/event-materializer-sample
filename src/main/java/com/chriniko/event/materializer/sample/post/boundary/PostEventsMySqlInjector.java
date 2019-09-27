package com.chriniko.event.materializer.sample.post.boundary;

import com.chriniko.event.materializer.sample.post.domain.Post;
import com.chriniko.event.materializer.sample.post.event.PostCreatedEvent;
import com.chriniko.event.materializer.sample.post.event.PostDeletedEvent;
import com.chriniko.event.materializer.sample.post.event.PostUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.Supplier;

@Component
public class PostEventsMySqlInjector {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void createPost(Post post) {

        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);

        PostCreatedEvent postCreatedEvent = PostCreatedEvent.of(post.getId(),
                post.getAuthor(),
                post.getDescription(),
                post.getText(),
                Instant.now()
        );

        String payloadAsJson = Try.of(() -> objectMapper.writeValueAsString(postCreatedEvent))
                .getOrElseThrow((Supplier<IllegalStateException>) IllegalStateException::new);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                jdbcTemplate.update(
                        "insert into test.post_events(creation_date, payloadAsJson, class) VALUES (?,?,?)",
                        Timestamp.from(Instant.now()),
                        payloadAsJson,
                        PostCreatedEvent.class.getName());

            }
        });
    }

    public void deletePost(Post post) {
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);

        PostDeletedEvent postDeletedEvent = PostDeletedEvent.of(post.getId());

        String payloadAsJson = Try.of(() -> objectMapper.writeValueAsString(postDeletedEvent))
                .getOrElseThrow((Supplier<IllegalStateException>) IllegalStateException::new);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                jdbcTemplate.update(
                        "insert into test.post_events(creation_date, payloadAsJson, class) VALUES (?,?,?)",
                        Timestamp.from(Instant.now()),
                        payloadAsJson,
                        PostDeletedEvent.class.getName());
            }
        });
    }

    public void updatePost(Post post) {

        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);

        PostUpdatedEvent postUpdatedEvent = PostUpdatedEvent.of(post.getId(),
                post.getAuthor(),
                post.getDescription(),
                post.getText());

        String payloadAsJson = Try.of(() -> objectMapper.writeValueAsString(postUpdatedEvent))
                .getOrElseThrow((Supplier<IllegalStateException>) IllegalStateException::new);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                jdbcTemplate.update(
                        "insert into test.post_events(creation_date, payloadAsJson, class) VALUES (?,?,?)",
                        Timestamp.from(Instant.now()),
                        payloadAsJson,
                        PostUpdatedEvent.class.getName());
            }
        });

    }
}
