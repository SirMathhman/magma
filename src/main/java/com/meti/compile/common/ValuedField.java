package com.meti.compile.common;

import com.meti.collect.EmptyStream;
import com.meti.collect.JavaList;
import com.meti.collect.Streams;
import com.meti.compile.attribute.Attribute;
import com.meti.compile.attribute.AttributeException;
import com.meti.compile.attribute.NodeAttribute;
import com.meti.compile.node.Node;
import com.meti.compile.node.Text;

public class ValuedField extends Field {
    private final Node value;

    public ValuedField(JavaList<Flag> flags, Text name, Node type, Node value) {
        super(flags, name, type);
        this.value = value;
    }

    @Override
    public Attribute apply(Attribute.Type type) throws AttributeException {
        return type == Attribute.Type.Value ? new NodeAttribute(value) : super.apply(type);
    }

    @Override
    public com.meti.collect.Stream<Attribute.Type> apply1(Attribute.Group group) throws AttributeException {
        return group == Attribute.Group.Node ? Streams.apply(Attribute.Type.Value) : new EmptyStream<>();
    }

    @Override
    public Node with(Attribute.Type type, Attribute attribute) throws AttributeException {
        return type == Attribute.Type.Value ? new ValuedField(flags, name, this.type, attribute.asNode()) : super.with(type, attribute);
    }

    @Override
    protected Field complete(Text name, Node type) {
        return new ValuedField(flags, this.name, type, value);
    }

    @Override
    public boolean is(Type type) {
        return type == Type.ValuedField;
    }
}
