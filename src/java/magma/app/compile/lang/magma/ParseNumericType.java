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
import magma.app.compile.pass.Passer;

public class ParseNumericType implements Passer {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        if (!node.is("numeric-type")) return new None<>();

        final var value = node.findString("value").orElse("");
        final var sign = value.charAt(0) == 'I' ? 1 : 0;
        final var bits = Integer.parseInt(value.substring(1));

        final var newNode = new MapNode("numeric-type")
                .withInt("signed", sign)
                .withInt("bits", bits);

        return new Some<>(new Ok<>(new Tuple<>(state, newNode)));
    }
}
