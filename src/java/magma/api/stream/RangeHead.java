package magma.api.stream;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

public class RangeHead implements Head<Integer> {
    private final int extent;
    private int counter = 0;

    public RangeHead(int extent) {
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
