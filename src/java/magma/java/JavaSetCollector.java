package magma.java;

import magma.api.stream.Collector;

class JavaSetCollector<T> implements Collector<T, JavaSet<T>> {
    @Override
    public JavaSet<T> createInitial() {
        return new JavaSet<>();
    }

    @Override
    public JavaSet<T> fold(JavaSet<T> current, T element) {
        return current.add(element);
    }
}
