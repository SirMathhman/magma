package com.meti.app.compile.clang;

import com.meti.api.option.None;
import com.meti.api.option.Option;
import com.meti.api.option.Some;
import com.meti.app.compile.attribute.Attribute;
import com.meti.app.compile.attribute.AttributeException;
import com.meti.app.compile.node.Node;
import com.meti.app.compile.node.Text;
import com.meti.app.compile.render.Renderer;

public record ExternRenderer(Node node) implements Renderer {
    @Override
    public Option<Text> render() throws AttributeException {
        if (node.is(Node.Type.Extern)) {
            return new Some<>(new Text("#include <" + node.apply(Attribute.Type.Value)
                    .asText().computeTrimmed() + ".h>\n"));
        } else {
            return new None<>();
        }
    }
}