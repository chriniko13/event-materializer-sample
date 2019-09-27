package com.chriniko.event.materializer.sample.post.domain;

import lombok.Data;

@Data
public class Post {

    private String id;

    private String author;
    private String description;
    private String text;

    private String createdAt;
    private String updatedAt;
}
