package magma;

import java.util.stream.Stream;

public class Options {
    public static <T> Stream<T> stream(Option<T> option) {
        return option.map(Stream::of).orElseGet(Stream::empty);
    }
}
