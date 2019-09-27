package com.chriniko.event.materializer.sample.post.init;

import com.chriniko.event.materializer.sample.core.Buffer;
import com.chriniko.event.materializer.sample.core.DataService;
import com.chriniko.event.materializer.sample.post.control.PostKafkaBuilder;
import com.chriniko.event.materializer.sample.post.control.PostMysqlBuilder;
import com.chriniko.event.materializer.sample.post.domain.Post;
import lombok.Getter;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;

@Component
public class PostsMaterializerManager {

    private final ObjectFactory<PostMysqlBuilder> postMysqlBuilderObjectFactory;
    private final ObjectFactory<PostKafkaBuilder> postKafkaBuilderObjectFactory;

    @Getter
    private Buffer<Post, String> mysqlPostBuffer;

    @Getter
    private Buffer<Post, String> kafkaPostBuffer;

    @Autowired
    public PostsMaterializerManager(ObjectFactory<PostMysqlBuilder> postMysqlBuilderObjectFactory,
                                    ObjectFactory<PostKafkaBuilder> postKafkaBuilderObjectFactory) {
        this.postMysqlBuilderObjectFactory = postMysqlBuilderObjectFactory;
        this.postKafkaBuilderObjectFactory = postKafkaBuilderObjectFactory;
    }

    @PostConstruct
    void init() {

        PostMysqlBuilder postMysqlBuilder = postMysqlBuilderObjectFactory.getObject();
        mysqlPostBuffer = postMysqlBuilder.getBuffer();

        PostKafkaBuilder postKafkaBuilder = postKafkaBuilderObjectFactory.getObject();
        kafkaPostBuffer = postKafkaBuilder.getBuffer();

        new DataService<Post, Long, String>(
                Collections.singletonList(postMysqlBuilder),
                2000)
                .start();

        new DataService<Post, String, String>(
                Collections.singletonList(postKafkaBuilder),
                2000)
                .start();
    }

}
