package com.meti.attribute;

import com.meti.feature.Node;

import java.util.stream.Stream;

public interface Attribute {
    default int asInteger() {
        throw new UnsupportedOperationException();
    }

    default Node asNode() {
        throw new UnsupportedOperationException();
    }

    default Stream<Node> asNodeStream() {
        throw new UnsupportedOperationException();
    }

    default String asString() {
        throw new UnsupportedOperationException();
    }

    enum Group {
        Node
    }

    enum Type {
        Children, Value
    }
}
