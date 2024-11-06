package magma.java;

import magma.api.stream.Collector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JavaLists {
    public static <T> List<T> add(List<T> list, T element) {
        var copy = new ArrayList<>(list);
        copy.add(element);
        return copy;
    }

    public static <T> Collector<T, List<T>> collector() {
        return new Collector<T, List<T>>() {
            @Override
            public List<T> fold(List<T> current, T element) {
                return add(current, element);
            }

            @Override
            public List<T> createInitial() {
                return Collections.emptyList();
            }
        };
    }
}
