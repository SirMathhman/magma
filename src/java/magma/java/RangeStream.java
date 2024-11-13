package magma.java;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.Head;

public final class RangeStream implements Head<Integer> {
    private final int extent;
    private int counter = 0;

    public RangeStream(int extent) {
        this.extent = extent;
    }

    @Override
    public Option<Integer> next() {
        if (counter >= extent) return new None<>();

        var value = counter;
        counter++;
        return new Some<>(value);
    }
}
