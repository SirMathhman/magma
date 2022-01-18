package com.meti.app.compile.attribute;

import com.meti.app.compile.node.Node;

public record NodeAttribute(Node node) implements Attribute {
    @Override
    public Node asNode() {
        return node;
    }
}