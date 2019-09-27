package com.chriniko.event.materializer.sample.core;

import java.util.Collection;

public abstract class Buffer<E, K> {

    public abstract String name();

    public abstract String toString();

    public abstract Collection<E> contents();

    public abstract E getOrCreate(K key);

    public abstract void delete(K key);

    public abstract E findById(K key);
}
