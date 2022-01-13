package com.meti.collect;

public class StreamException extends CollectionException {
    public StreamException(String message) {
        super(message);
    }

    public StreamException(Exception cause) {
        super(cause);
    }
}