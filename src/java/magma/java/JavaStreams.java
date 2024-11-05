package magma.java;

import magma.api.stream.HeadedStream;
import magma.api.stream.Stream;

import java.util.List;

public class JavaStreams {
    public static <T> Stream<T> stream(List<T> list) {
        return new HeadedStream<T>(new NativeListHead<>(list));
    }
}
