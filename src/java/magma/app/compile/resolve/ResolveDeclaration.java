package magma.app.compile.resolve;

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
import magma.app.compile.pass.Passer;

import static magma.app.compile.lang.common.CommonLang.NUMERIC_TYPE_TYPE;
import static magma.app.compile.lang.magma.MagmaLang.DECLARATION_TYPE;
import static magma.app.compile.lang.magma.MagmaLang.DECLARATION_TYPE_PROPERTY;

public class ResolveDeclaration implements Passer {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        if (!node.is(DECLARATION_TYPE)) return new None<>();

        final var type = new MapNode(NUMERIC_TYPE_TYPE)
                .withInt("sign", 0)
                .withInt("bits", 32);

        final var node1 = node.withNode(DECLARATION_TYPE_PROPERTY, type);
        return new Some<>(new Ok<>(new Tuple<>(state, node1)));
    }
}
