package magma.api.stream;

import magma.api.option.None;
import magma.api.option.Option;

import java.util.function.Function;

class FlatMapHead<T, R> implements Head<R> {
    private final Function<T, Stream<R>> mapper;
    private final Head<T> parent;
    private Stream<R> current;

    public FlatMapHead(Head<T> parent, Stream<R> inner, Function<T, Stream<R>> mapper) {
        this.mapper = mapper;
        this.current = inner;
        this.parent = parent;
    }

    @Override
    public Option<R> next() {
        while (true) {
            final var next = this.current.next();
            if (next.isPresent()) return next;

            final var tuple = this.parent.next()
                    .map(this.mapper)
                    .toTuple(this.current);

            if (tuple.left()) {
                this.current = tuple.right();
            } else {
                return new None<>();
            }
        }
    }
}
