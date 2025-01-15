package magma;

import java.util.function.BiFunction;
import java.util.function.Function;

public record Tuple<A, B>(A left, B right) {
    public static <A, B, C> Function<Tuple<A, B>, C> merge(BiFunction<A, B, C> merger) {
        return abTuple -> merger.apply(abTuple.left, abTuple.right);
    }
}
