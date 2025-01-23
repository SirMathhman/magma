package magma.app;

import magma.api.result.Result;
import magma.app.error.CompileError;

import java.util.function.Predicate;

public interface Passer {
    static Predicate<Node> by(String type) {
        return value -> value.is(type);
    }

    Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit);

    Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit);
}
