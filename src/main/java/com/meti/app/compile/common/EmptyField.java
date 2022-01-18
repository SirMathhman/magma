package com.meti.app.compile.common;

import com.meti.api.collect.JavaList;
import com.meti.app.compile.node.Node;
import com.meti.app.compile.node.Text;

public class EmptyField extends Field {
    public EmptyField(Text name, Node type, Flag... flags) {
        this(name, type, JavaList.apply(flags));
    }

    public EmptyField(Text name, Node type, JavaList<Flag> flags) {
        super(flags, name, type);
    }

    @Override
    protected Field complete(Text name, Node type) {
        return new EmptyField(name, type, flags);
    }

    @Override
    public boolean is(Type type) {
        return type == Type.Declaration;
    }
}