package magma.app.compile;

import magma.api.option.Option;
import magma.api.result.Result;
import magma.api.stream.Stream;
import magma.app.compile.error.CompileError;
import magma.java.JavaStreams;

import java.util.List;
import java.util.function.Function;

public record CompoundPasser(List<Passer> passers) implements Passer {
    @Override
    public Option<Result<Node, CompileError>> pass(Node type) {
        final var map = JavaStreams.fromList(passers)
                .map(passer -> passer.pass(type));
        return map
                .flatMap(new Function<Option<Result<Node, CompileError>>, Stream<Result<Node, CompileError>>>() {
                    @Override
                    public Stream<Result<Node, CompileError>> apply(Option<Result<Node, CompileError>> resultOption) {
                        return Stream.fromOption(resultOption);
                    }
                })
                .next();
    }
}
