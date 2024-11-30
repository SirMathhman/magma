package magma.app.compile.pass;

import magma.api.option.Option;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.java.JavaList;

public record CompoundPasser(JavaList<Passer> passers) implements Passer {
    @Override
    public Option<Result<Node, CompileError>> beforeNode(Node node) {
        return passers.stream()
                .map(passer -> passer.beforeNode(node))
                .flatMap(Streams::fromOption)
                .next();
    }

    @Override
    public Option<Result<Node, CompileError>> afterNode(Node node) {
        return passers.stream()
                .map(passer -> passer.afterNode(node))
                .flatMap(Streams::fromOption)
                .next();
    }
}