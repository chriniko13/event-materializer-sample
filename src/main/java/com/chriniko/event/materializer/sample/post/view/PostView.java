package com.chriniko.event.materializer.sample.post.view;


import com.chriniko.event.materializer.sample.jsf.JsfEngine;
import com.chriniko.event.materializer.sample.post.boundary.PostEventsKafkaInjector;
import com.chriniko.event.materializer.sample.post.boundary.PostEventsMySqlInjector;
import com.chriniko.event.materializer.sample.post.domain.Post;
import com.chriniko.event.materializer.sample.post.init.PostsMaterializerManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2

@Named
@ViewScoped
public class PostView implements Serializable {

    @Autowired
    private JsfEngine jsfEngine;

    @Autowired
    private PostEventsMySqlInjector postEventsMySqlInjector;

    @Autowired
    private PostEventsKafkaInjector postEventsKafkaInjector;

    @Autowired
    private PostsMaterializerManager postsMaterializerManager;

    @Getter
    private Post newPost;

    @Getter
    private List<Post> posts;

    @Getter
    private List<Post> postsFromKafkaSource;

    @Getter
    @Setter
    private Post selectedPost;

    @PostConstruct
    public void init() {
        newPost = new Post();

        posts = new ArrayList<>(postsMaterializerManager.getMysqlPostBuffer().contents());
        postsFromKafkaSource = new ArrayList<>(postsMaterializerManager.getKafkaPostBuffer().contents());

        posts.forEach(post -> log.info(post.toString()));
    }

    public void refresh() {
        posts = new ArrayList<>(postsMaterializerManager.getMysqlPostBuffer().contents());
        postsFromKafkaSource = new ArrayList<>(postsMaterializerManager.getKafkaPostBuffer().contents());

        jsfEngine.displayMessage("Posts datatable refreshed successfully!");
    }

    public void insertNewPost() {

        newPost.setId(UUID.randomUUID().toString());
        postEventsMySqlInjector.createPost(newPost);
        postEventsKafkaInjector.createPost(newPost);

        posts = new ArrayList<>(postsMaterializerManager.getMysqlPostBuffer().contents());

        jsfEngine.displayMessage("Post with description: " + newPost.getDescription() + " saved successfully!");

        newPost = new Post();
    }

    public void deleteSelectedPost() {
        if (postIsNotSelected()) {
            return;
        }

        Post toDeleteFromMysql = postsMaterializerManager.getMysqlPostBuffer().findById(selectedPost.getId());
        Post toDeleteFromKafka = postsMaterializerManager.getKafkaPostBuffer().findById(selectedPost.getId());

        postEventsMySqlInjector.deletePost(toDeleteFromMysql);
        postEventsKafkaInjector.deletePost(toDeleteFromKafka);

        posts = new ArrayList<>(postsMaterializerManager.getMysqlPostBuffer().contents());

        jsfEngine.displayMessage("Post with id: " + selectedPost.getId() + " deleted successfully!");

        selectedPost = null;
    }

    public void updateSelectedPost() {
        if (postIsNotSelected()) {
            return;
        }

        Post toUpdateFromMysql = postsMaterializerManager.getMysqlPostBuffer().findById(selectedPost.getId());

        postEventsMySqlInjector.updatePost(toUpdateFromMysql);
        postEventsKafkaInjector.updatePost(toUpdateFromMysql);

        jsfEngine.displayMessage("Post with id: " + selectedPost.getId() + " updated successfully!");

        selectedPost = null;
    }

    private boolean postIsNotSelected() {
        if (selectedPost == null) {
            jsfEngine.displayWarnMessage("Please first select a post!");
            return true;
        }
        return false;
    }

}
