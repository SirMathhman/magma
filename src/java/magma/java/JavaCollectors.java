package magma.java;

import magma.api.stream.Collector;

import java.util.List;

public class JavaCollectors {
    public static <T> Collector<T, List<T>> asList() {
        return new Collector<T, List<T>>() {
        };
    }
}
