package com.meti.app.compile.declare;

import com.meti.app.compile.CompileException;
import com.meti.app.compile.Node;
import com.meti.app.compile.Renderer;
import com.meti.app.compile.attribute.Attribute;
import com.meti.core.Ok;
import com.meti.core.Option;
import com.meti.core.Result;
import com.meti.java.String_;

import static com.meti.core.Options.$Option;
import static com.meti.java.JavaString.fromSlice;

public record DeclarationRenderer(Node node) implements Renderer {
    @Override
    public Option<Result<String_, CompileException>> render() {
        return $Option(() -> {
            var name = node.applyOptionally(fromSlice("name")).flatMap(Attribute::asString).$();
            Node node1 = node.applyOptionally(fromSlice("type")).flatMap(attribute -> attribute.asNode().map(value -> value.b())).$();
            var type = node1.applyOptionally(fromSlice("value")).flatMap(Attribute::asString).$();

            return name.append(" : ").appendOwned(type);
        }).map(Ok::apply);
    }
}
