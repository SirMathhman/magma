package magma.app.compile.pass;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.java.JavaList;

public final class CompoundPasser implements Passer {
    private final JavaList<Passer> passers;

    public CompoundPasser(JavaList<Passer> passers) {
        this.passers = passers;
    }

    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        return passers.stream()
                .map(passer -> passer.afterPass(state, node))
                .flatMap(Streams::fromOption)
                .next();
    }

    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node) {
        return passers.stream()
                .map(passer -> passer.beforePass(state, node))
                .flatMap(Streams::fromOption)
                .next();
    }
}
