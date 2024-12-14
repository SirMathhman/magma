package magma.api;

import java.util.function.BiFunction;

public record Tuple<A, B>(A left, B right) {
    public <R> R merge(BiFunction<A, B, R> folder) {
        return folder.apply(left, right);
    }
}
