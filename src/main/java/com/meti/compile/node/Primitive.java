package com.meti.compile.node;

import com.meti.compile.attribute.Attribute;
import com.meti.compile.attribute.AttributeException;
import com.meti.compile.attribute.TextAttribute;

public enum Primitive implements Node {
    Void;

    @Override
    public Attribute apply(Attribute.Type type) throws AttributeException {
        if(type == Attribute.Type.Name) return new TextAttribute(new Text(name()));
        throw new AttributeException(type);
    }

    @Override
    public boolean is(Type type) {
        return type == Type.Primitive;
    }
}