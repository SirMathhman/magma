package magma.api.stream;

import java.util.function.BiFunction;

public interface Stream<T> {
    <C> C collect(Collector<T, C> collector);

    <C> C foldRight(C initial, BiFunction<C, T, C> folder);
}
