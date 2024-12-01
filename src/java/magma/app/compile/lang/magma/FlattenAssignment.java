package magma.app.compile.lang.magma;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.java.JavaList;

public class FlattenAssignment implements magma.app.compile.pass.Passer {
    @Override
    public Option<Result<Node, CompileError>> beforeNode(Node node) {
        if (!node.is(MagmaLang.ASSIGNMENT_TYPE)) return new None<>();

        final var variable = node.findNode(MagmaLang.ASSIGNMENT_VARIABLE).orElse(new MapNode());
        final var expression = node.findNode(MagmaLang.ASSIGNMENT_EXPRESSION).orElse(new MapNode());

        return new Some<>(new Ok<>(CommonLang.toGroup(new JavaList<Node>()
                .add(new MapNode("move").withNode("value", variable))
                .add(new MapNode("store").withNode("value", expression)))));
    }
}
