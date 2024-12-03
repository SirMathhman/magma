package magma.app.compile.pass;

import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.StringContext;
import magma.app.compile.rule.Rule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record SavingPassingStage(Path path, Rule rule) implements PassingStage {
    @Override
    public Result<Tuple<State, Node>, CompileError> pass(State state, Node root) {
        return rule.generate(root).flatMapValue(value -> getObjectCompileErrorResult(state, root, value));
    }

    private Result<Tuple<State, Node>, CompileError> getObjectCompileErrorResult(State state, Node root, String value) {
        try {
            Files.writeString(path, value);
            return new Ok<>(new Tuple<>(state, root));
        } catch (IOException e) {
            return new Err<>(new CompileError(e.getMessage(), new StringContext(path.toString())));
        }
    }
}
