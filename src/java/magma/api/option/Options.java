package magma.api.option;

import java.util.stream.Stream;

public class Options {
    public static <T> Stream<T> asStream(Option<T> option) {
        return option.map(Stream::of).orElseGet(Stream::empty);
    }
}
