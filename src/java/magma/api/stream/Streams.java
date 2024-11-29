package magma.api.stream;

import magma.api.option.Option;

public class Streams {
    public static <T> Stream<T> fromOption(Option<T> option) {
        return new HeadedStream<>(option.<Head<T>>map(SingleHead::new).orElseGet(EmptyHead::new));
    }
}
