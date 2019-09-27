package com.chriniko.event.materializer.sample.post.control;

import com.chriniko.event.materializer.sample.core.Buffer;
import com.chriniko.event.materializer.sample.post.domain.Post;
import lombok.extern.log4j.Log4j2;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
@NotThreadSafe
public class PostBuffer extends Buffer<Post, String> {

    private final String bufferName;

    private final Map<String, Post> postsById;

    public PostBuffer(String bufferName) {
        this.bufferName = bufferName;
        this.postsById = new LinkedHashMap<>();
        log.info("created buffer with name: " + bufferName);
    }

    @Override
    public String name() {
        return this.bufferName;
    }

    @Override
    public String toString() {
        return "name: " + bufferName + " --- buffer count: " + count();
    }

    @Override
    public Collection<Post> contents() {
        return new ArrayList<>(postsById.values());
    }

    @Override
    public Post getOrCreate(String id) {
        return postsById.computeIfAbsent(id, s -> {
            Post p = new Post();
            p.setId(id);
            return p;
        });
    }

    @Override
    public void delete(String id) {
        postsById.remove(id);
    }

    @Override
    public Post findById(String id) {
        return postsById.get(id);
    }

    private long count() {
        return postsById.size();
    }

}
