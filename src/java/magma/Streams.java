package magma;

import java.util.List;

public class Streams {
    public static <T> Stream<T> from(List<T> list) {
        return new HeadedStream<>(new NativeListHead<>(list));
    }
}
