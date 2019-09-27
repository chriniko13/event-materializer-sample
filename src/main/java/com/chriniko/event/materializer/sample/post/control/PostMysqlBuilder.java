package com.chriniko.event.materializer.sample.post.control;

import com.chriniko.event.materializer.sample.core.Buffer;
import com.chriniko.event.materializer.sample.core.Builder;
import com.chriniko.event.materializer.sample.core.BuilderProcessingException;
import com.chriniko.event.materializer.sample.post.domain.Post;
import com.chriniko.event.materializer.sample.post.event.PostEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.List;

@Log4j2

@Component
@Scope("prototype")
public class PostMysqlBuilder extends Builder<Post, Long, String> {

    // Note: walk pace, small number here to see-observe the synchronization of data service.
    private static final int LIMIT = 500;

    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final PostEventCalculator postEventCalculator;

    @Autowired
    public PostMysqlBuilder(Buffer<Post, String> buffer,
                            ObjectMapper objectMapper,
                            JdbcTemplate jdbcTemplate,
                            TransactionTemplate transactionTemplate,
                            PostEventCalculator postEventCalculator) {
        super(buffer);
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.postEventCalculator = postEventCalculator;
    }

    @Override
    protected Long getInitialOffset() {
        return 0L;
    }

    @Override
    protected Long synchronizeLogic() {
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);

        List<PostEvent> postEvents = transactionTemplate.execute(new TransactionCallback<List<PostEvent>>() {
            @Override
            public List<PostEvent> doInTransaction(TransactionStatus transactionStatus) {

                return jdbcTemplate.query(
                        "SELECT id, creation_date, payloadAsJson, class FROM test.post_events ORDER BY id ASC LIMIT ? OFFSET ?",
                        new Object[]{LIMIT, offset},
                        (rs, rowNum) -> {
                            try {
                                String payloadAsJson = rs.getString("payloadAsJson");
                                return objectMapper.readValue(payloadAsJson, PostEvent.class);
                            } catch (IOException e) {
                                throw new BuilderProcessingException("could not deserialize post event", e);
                            }
                        }
                );
            }
        });

        int postEventsSize = Try.of(postEvents::size).getOrElse(0);

        if (postEventsSize > 0) {
            for (PostEvent postEvent : postEvents) {
                postEventCalculator.calculateState(postEvent, buffer);
            }

            long newPosition = offset + postEventsSize;
            log.info("processed post events: {}, new offset is: {}", postEventsSize, newPosition);
            return newPosition;

        } else {
            return offset;
        }
    }

}
