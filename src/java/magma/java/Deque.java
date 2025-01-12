package magma.java;

import magma.Tuple;
import magma.option.Option;

public interface Deque<T> {
    default Deque<T> add(T next) {
        this.list.add(next);
        return this;
    }

    boolean isEmpty();

    Option<Tuple<T, Deque<T>>> pop();

    Option<T> peek();
}
