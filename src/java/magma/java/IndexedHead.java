package magma.java;

import magma.core.option.None;
import magma.core.option.Option;
import magma.core.stream.Head;

public class IndexedHead<T> implements Head<T> {
    private final Indexed<T> indexed;
    private int counter = 0;

    public IndexedHead(Indexed<T> indexed) {
        this.indexed = indexed;
    }

    @Override
    public Option<T> next() {
        if (counter >= indexed.size()) return new None<>();

        final var element = indexed.get(counter);
        counter++;
        return element;
    }
}
