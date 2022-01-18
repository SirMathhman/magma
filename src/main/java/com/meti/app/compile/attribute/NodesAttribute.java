package com.meti.app.compile.attribute;

import com.meti.api.collect.JavaList;
import com.meti.app.compile.node.Node;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Deprecated
public record NodesAttribute(Collection<Node> values) implements Attribute {
    @Override
    public Stream<Node> asStreamOfNodes() {
        return values.stream();
    }

    @Override
    public com.meti.api.collect.Stream<Node> asStreamOfNodes1() throws AttributeException {
        return new JavaList<>(asStreamOfNodes().collect(Collectors.toList())).stream();
    }
}