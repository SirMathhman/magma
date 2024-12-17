package magma.api.collect;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.Head;

public class RangeHead implements Head<Integer> {
    private final int extent;
    private int counter = 0;

    public RangeHead(int extent) {
        this.extent = extent;
    }

    @Override
    public Option<Integer> next() {
        if (counter < extent) {
            var value = counter;
            counter++;
            return new Some<>(value);
        } else {
            return new None<>();
        }
    }
}
