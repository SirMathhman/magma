package com.meti.app.compile.block;

import com.meti.app.compile.CompileException;
import com.meti.app.compile.Node;
import com.meti.app.compile.Renderer;
import com.meti.app.compile.attribute.Attribute;
import com.meti.core.Ok;
import com.meti.core.Option;
import com.meti.core.Result;
import com.meti.iterate.Iterators;
import com.meti.java.JavaString;
import com.meti.java.List;
import com.meti.java.String_;

import static com.meti.java.JavaString.fromSlice;

public record BlockRenderer(Node block) implements Renderer {
    private String_ renderContent(List<? extends Node> content) {
        return content.iter()
                .map(node -> node.applyOptionally(fromSlice("value")).flatMap(Attribute::asString))
                .flatMap(Iterators::fromOption)
                .collect(JavaString.joining(fromSlice("")))
                .unwrapOrElse(fromSlice(""))
                .prepend("{")
                .append("}");
    }

    @Override
    public Option<Result<String_, CompileException>> render() {
        return block.applyOptionally(fromSlice("lines"))
                .flatMap(attribute -> attribute.asListOfNodes().map(value -> value.b()))
                .map(this::renderContent)
                .map(Ok::apply);
    }
}