package magma.java;

import magma.api.stream.Collector;

public class JavaListCollector<T> implements Collector<T, JavaList<T>> {
    @Override
    public JavaList<T> createInitial() {
        return new JavaList<>();
    }

    @Override
    public JavaList<T> fold(JavaList<T> current, T element) {
        return current.add(element);
    }
}
