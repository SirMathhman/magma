package magma;

import java.util.function.BiFunction;

public record Tuple<A, B>(A left, B right) {
    public <R> Tuple<R, B> mergeIntoLeft(BiFunction<A, B, R> merger) {
        return new Tuple<>(merger.apply(this.left, this.right), this.right);
    }
}
