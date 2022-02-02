package com.meti.app.compile.common;

import com.meti.api.collect.java.List;
import com.meti.api.collect.stream.StreamException;
import com.meti.api.json.ArrayNode;
import com.meti.api.json.JSONException;
import com.meti.api.json.JSONNode;
import com.meti.api.json.ObjectNode;
import com.meti.app.compile.node.AbstractNode;
import com.meti.app.compile.node.Node;
import com.meti.app.compile.node.attribute.*;
import com.meti.app.compile.text.Input;
import com.meti.app.compile.text.RootText;

import java.util.Objects;

public abstract class Definition extends AbstractNode {
    protected final List<Flag> flags;
    protected final Input name;
    protected final Node type;

    public Definition(List<Flag> flags, Input name, Node type) {
        this.flags = flags;
        this.name = name;
        this.type = type;
    }

    @Override
    public Attribute apply(Attribute.Type type) throws AttributeException {
        return switch (type) {
            case Name -> new InputAttribute(name);
            case Flags -> new FlagsAttribute(flags);
            case Type -> new NodeAttribute(this.type);
            default -> throw new AttributeException(this, type);
        };
    }

    @Override
    public Node with(Attribute.Type type, Attribute attribute) throws AttributeException {
        if (type == Attribute.Type.Name) {
            Input input;
            try {
                input = attribute.asInput();
            } catch (AttributeException e) {
                // TODO: figure out a better conversion than this
                input = new RootText(attribute.asOutput().compute());
            }

            return complete(input, this.type);
        }
        if (type == Attribute.Type.Type) {
            return complete(name, attribute.asNode());
        }
        return this;
    }

    @Override
    public JSONNode toJSON() throws JSONException {
        try {
            var flags = this.flags.stream()
                    .map(Flag::toString)
                    .foldRight(new ArrayNode.Builder(), ArrayNode.Builder::addString)
                    .build();
            return new ObjectNode()
                    .addString("name", name.toOutput().compute())
                    .addJSONable("type", type)
                    .addObject("flags", flags);
        } catch (StreamException e) {
            return new ObjectNode();
        }
    }

    protected abstract Definition complete(Input name, Node type) throws AttributeException;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Definition definition)) return false;
        return Objects.equals(flags, definition.flags) && Objects.equals(name, definition.name) && Objects.equals(type, definition.type);
    }

    public enum Flag {
        Extern,
        Operator,
        Let, Const, Def
    }
}
