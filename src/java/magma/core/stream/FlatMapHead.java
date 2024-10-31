package magma.core.stream;

import magma.core.option.None;
import magma.core.option.Option;

import java.util.function.Function;

class FlatMapHead<T, R> implements Head<R> {
    private final Function<T, Head<R>> mapper;
    private final Head<T> head;
    private Head<R> current;

    public FlatMapHead(Head<T> head, Head<R> current, Function<T, Head<R>> mapper) {
        this.head = head;
        this.current = current;
        this.mapper = mapper;
    }

    @Override
    public Option<R> next() {
        while (true) {
            final var next = current.next();
            if (next.isPresent()) return next;

            final var tuple = head.next()
                    .map(mapper)
                    .toTuple(current);

            if (tuple.left()) {
                current = tuple.right();
            } else {
                return new None<>();
            }
        }
    }
}
