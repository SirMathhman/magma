package magma.java;

import magma.api.stream.Collector;

import java.util.List;

public record JavaList<T>(List<T> hidden) implements magma.java.List<T> {
    public static <T> Collector<T, magma.java.List<T>> collector() {
        return new Collector<T, magma.java.List<T>>() {
        };
    }
}
