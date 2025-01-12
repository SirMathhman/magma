package magma.java;

import magma.Tuple;
import magma.collect.Deque;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.stream.Collector;

import java.util.LinkedList;

public class JavaLinkedList<T> implements Deque<T> {
    private final LinkedList<T> list;

    public JavaLinkedList() {
        this(new LinkedList<>());
    }

    public JavaLinkedList(LinkedList<T> list) {
        this.list = list;
    }

    public static <T> Collector<T, Deque<T>> collector() {
        return new Collector<>() {
            @Override
            public Deque<T> createInitial() {
                return new JavaLinkedList<>();
            }

            @Override
            public Deque<T> fold(Deque<T> current, T next) {
                return current.add(next);
            }
        };
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    @Override
    public Option<Tuple<T, Deque<T>>> pop() {
        return isEmpty() ? new None<>() : new Some<>(new Tuple<>(this.list.pop(), this));
    }

    @Override
    public Option<T> peek() {
        return isEmpty() ? new None<>() : new Some<>(this.list.peek());
    }

    @Override
    public Deque<T> add(T next) {
        this.list.add(next);
        return this;
    }
}
