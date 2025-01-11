package magma.stream;

import magma.option.Option;

public class Streams {
    public static <T> Stream<T> from(Option<T> option) {
        return new HeadedStream<>(option
                .<Head<T>>map(ArrayHead::new)
                .orElseGet(EmptyHead::new));
    }
}
