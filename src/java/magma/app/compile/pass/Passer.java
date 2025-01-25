package magma.app.compile.pass;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.node.Node;
import magma.app.error.CompileError;

import java.util.function.Predicate;

public interface Passer {
    static Predicate<Node> by(String type) {
        return value -> value.is(type);
    }

    default Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return new Ok<>(unit);
    }

    default Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit);
    }
}
