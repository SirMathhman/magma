package magma.app.compile.lang.magma;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.pass.Passer;
import magma.java.JavaList;

import static magma.app.compile.lang.magma.MagmaLang.*;

public class FlattenDeclaration implements Passer {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
        if (!node.is(DECLARATION_TYPE)) return new None<>();

        final var name = node.findString(DECLARATION_NAME).orElse("");
        final var type = node.findNode(DECLARATION_TYPE_PROPERTY).orElse(new MapNode());
        final var value = node.findNode(DECLARATION_VALUE).orElse(new MapNode());

        final var definition = new MapNode(DEFINITION_TYPE)
                .withString(DEFINITION_NAME, name)
                .withNode(DEFINITION_TYPE_PROPERTY, type);

        final var symbol = new MapNode("symbol")
                .withString("value", name);

        final var assignment = new MapNode(ASSIGNMENT_TYPE)
                .withNode(ASSIGNMENT_VARIABLE, symbol)
                .withNode(ASSIGNMENT_EXPRESSION, value);

        final var children = new JavaList<Node>()
                .add(definition)
                .add(assignment);

        return new Some<>(new Ok<>(new Tuple<>(state, CommonLang.toGroup(children))));
    }

}
