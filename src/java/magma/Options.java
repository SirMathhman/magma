package magma;

import magma.option.Option;

import java.util.stream.Stream;

public class Options {
    public static <T> Stream<T> asStream(Option<T> option) {
        return option.<Stream<T>>map(Stream::of).orElseGet(Stream::empty);
    }
}
