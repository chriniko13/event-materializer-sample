package com.chriniko.event.materializer.sample.core;

import lombok.Getter;

public abstract class Builder<T, O, K> {

    @Getter
    protected final Buffer<T, K> buffer;

    protected O offset;

    public Builder(Buffer<T, K> buffer) {
        this.buffer = buffer;
        this.offset = getInitialOffset();
    }

    protected abstract O getInitialOffset();

    protected abstract O synchronizeLogic();

    protected void synchronize() {
        offset = synchronizeLogic();
    }
}
