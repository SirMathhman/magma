package magma.api;

import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;

import java.util.function.BiFunction;

public record Tuple<A, B>(A left, B right) {
    public <R> R merge(BiFunction<A, B, R> merger) {
        return merger.apply(left, right);
    }
}
