package magma.java;

import magma.api.stream.HeadedStream;
import magma.api.stream.Stream;

import java.util.List;

public class JavaStreams {
    public static <T> Stream<T> stream(List<T> list) {
        return new HeadedStream<>(new NativeListHead<>(list));
    }

    public static Stream<Character> fromString(String input) {
        return new HeadedStream<>(new NativeStringHead(input));
    }
}
