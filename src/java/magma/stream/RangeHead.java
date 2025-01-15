package magma.stream;

import java.util.Optional;

public class RangeHead implements Head<Integer> {
    private final int size;
    private int counter = 0;

    public RangeHead(int size) {
        this.size = size;
    }

    @Override
    public Optional<Integer> next() {
        if (this.counter < this.size) {
            final var value = this.counter;
            this.counter++;
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }
}
