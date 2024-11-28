package magma.stream;

import magma.Node;
import magma.option.Option;

import java.util.List;

public class Streams {
    public static <T> Stream<T> from(List<T> list) {
        return new HeadedStream<>(new ListHead<>(list));
    }
}
