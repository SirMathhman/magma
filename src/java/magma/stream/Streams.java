package magma.stream;

public class Streams {
    @SafeVarargs
    public static <T> Stream<T> of(T... values) {
        return new HeadedStream<>(new RangeHead(values.length))
                .map(index -> values[index]);
    }
}