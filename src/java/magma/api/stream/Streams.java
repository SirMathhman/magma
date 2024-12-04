package magma.api.stream;

import magma.api.option.Option;
import magma.api.stream.head.*;

import java.util.List;

public class Streams {
    public static <T> Stream<T> from(List<T> list) {
        return new HeadedStream<>(new ListHead<>(list));
    }

    public static <T> Stream<T> fromOption(Option<T> option) {
        return new HeadedStream<>(option
                .<Head<T>>map(SingleHead::new)
                .orElseGet(EmptyHead::new));
    }
}