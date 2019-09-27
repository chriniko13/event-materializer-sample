package com.chriniko.event.materializer.sample.core;

public class BuilderProcessingException extends RuntimeException {

    public BuilderProcessingException(String msg, Throwable error) {
        super(msg, error);
    }
}
