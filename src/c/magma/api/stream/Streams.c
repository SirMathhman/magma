package magma.api.stream;package java.util.List;public class Streams {@SafeVarargs
    public static <T> Stream<T> of(T... values){return new HeadedStream<>(new RangeHead(values.length))
                .map(index -> values[index]);}public static <T> Stream<T> from(List<T> list){return new HeadedStream<>(new RangeHead(list.size()))
                .map(list::get);}}