package com.meti.compile;

import com.meti.collect.StreamException;
import com.meti.compile.attribute.Attribute;
import com.meti.compile.attribute.AttributeException;
import com.meti.compile.attribute.NodeAttribute;
import com.meti.compile.common.integer.IntegerNode;
import com.meti.compile.node.Node;
import com.meti.option.None;
import com.meti.option.Option;
import com.meti.option.Some;

public record CMagmaTransformer() {
    Node transform(Node node) throws CompileException {
        var withChild = transformNodeGroup(node);
        return transformBoolean(withChild).orElse(withChild);
    }

    private Option<Node> transformBoolean(Node node) throws TransformationException {
        if (node.is(Node.Type.Boolean)) {
            try {
                var value = node.apply(Attribute.Type.Value);
                var state = value.asBoolean();
                return new Some<>(new IntegerNode(state ? 1 : 0));
            } catch (AttributeException e) {
                throw new TransformationException(e);
            }
        }
        return new None<>();
    }

    private Node transformNodeAttribute(Node current, Attribute.Type type) throws CompileException {
        var previousAttribute = current.apply(type);
        var previous = previousAttribute.asNode();
        var next = transform(previous);
        var nextAttribute = new NodeAttribute(next);
        return current.with(type, nextAttribute);
    }

    private Node transformNodeGroup(Node node) throws CompileException {
        try {
            return node.apply1(Attribute.Group.Node).foldRight(node, this::transformNodeAttribute);
        } catch (StreamException e) {
            throw new CompileException(e);
        }
    }
}
