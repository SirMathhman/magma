package magma.java;

import magma.api.stream.Collector;

import java.util.ArrayList;
import java.util.List;

public class JavaCollectors {
    public static <T> Collector<T, List<T>> asList() {
        return new Collector<T, List<T>>() {
            @Override
            public List<T> initial() {
                return new ArrayList<>();
            }

            @Override
            public List<T> fold(List<T> current, T next) {
                var copy = new ArrayList<>(current);
                copy.add(next);
                return copy;
            }
        };
    }
}
