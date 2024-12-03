package magma.stream;

import magma.Error;
import magma.option.Option;

import java.util.List;

public class Streams {
    public static <T> Stream<T> from(List<T> list) {
        return new HeadedStream<>(new ListHead<>(list));
    }

    public static <T> Stream<T> fromOption(Option<T> option) {
        throw new UnsupportedOperationException();
    }
}
