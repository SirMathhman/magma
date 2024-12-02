package magma.app.compile.lang.magma;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.pass.Stateful;

public class FilteredStateless implements Stateful {
    private final Stateless stateless;
    private final String type;

    public FilteredStateless(String type, Stateless stateless) {
        this.stateless = stateless;
        this.type = type;
    }

    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
        if (!node.is(type)) return new None<>();
        final var newNode = stateless.beforePass(node);
        return new Some<>(new Ok<>(new Tuple<>(state, newNode)));
    }

    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        if (!node.is(type)) return new None<>();
        final var newNode = stateless.afterPass(node);
        return new Some<>(new Ok<>(new Tuple<>(state, newNode)));
    }
}
