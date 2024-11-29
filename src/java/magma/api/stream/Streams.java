package magma.api.stream;

import java.util.List;

public class Streams {
    public static <T> Stream<T> from(List<T> list) {
        return new HeadedStream<>(new ListHead<>(list));
    }

    @SafeVarargs
    public static <T> Stream<T> of(T... values) {
        return from(List.of(values));
    }
}
