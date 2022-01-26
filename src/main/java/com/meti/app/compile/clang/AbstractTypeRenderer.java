package com.meti.app.compile.clang;

import com.meti.api.option.None;
import com.meti.api.option.Option;
import com.meti.api.option.Some;
import com.meti.app.compile.node.Node;
import com.meti.app.compile.node.attribute.AttributeException;
import com.meti.app.compile.process.Processor;
import com.meti.app.compile.stage.CompileException;
import com.meti.app.compile.text.Input;
import com.meti.app.compile.text.Output;

public abstract class AbstractTypeRenderer implements Processor<Output> {
    protected final Input name;
    protected final Node type;
    protected final Node.Type nodeType;

    public AbstractTypeRenderer(Input name, Node type) {
        this.name = name;
        this.type = type;
        nodeType = Node.Type.Structure;
    }

    @Override
    public Option<Output> process() throws CompileException {
        return type.is(nodeType) ? new Some<>(processValid()) : new None<Output>();
    }

    protected abstract Output processValid() throws AttributeException;
}
