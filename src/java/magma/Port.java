package magma;

import java.util.List;
import java.util.Optional;

public class Port {
    private final List<Long> buffer;
    private int counter = 0;

    public Port(List<Long> buffer) {
        this.buffer = buffer;
    }

    public Optional<Long> read() {
        if (counter >= buffer.size()) return Optional.empty();

        final var value = buffer.get(counter);
        counter++;
        return Optional.of(value);
    }
}
