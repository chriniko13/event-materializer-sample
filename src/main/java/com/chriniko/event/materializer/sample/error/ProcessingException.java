package com.chriniko.event.materializer.sample.error;

public class ProcessingException extends RuntimeException {

    public ProcessingException(Throwable error) {
        super(error);
    }

}
