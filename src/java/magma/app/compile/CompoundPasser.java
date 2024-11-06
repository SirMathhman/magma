package magma.app.compile;

import magma.api.option.Option;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.compile.error.CompileError;
import magma.java.JavaStreams;

import java.util.List;

public record CompoundPasser(List<Passer> passers) implements Passer {
    @Override
    public Option<Result<Node, CompileError>> afterPass(Node node) {
        return JavaStreams.fromList(passers)
                .map(passer -> passer.afterPass(node))
                .flatMap(Streams::fromOption)
                .next();
    }

    @Override
    public Option<Result<Node, CompileError>> beforePass(Node node) {
        return JavaStreams.fromList(passers)
                .map(passer -> passer.beforePass(node))
                .flatMap(Streams::fromOption)
                .next();
    }
}
