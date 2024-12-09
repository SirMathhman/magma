package magma;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record Memory(List<Long> memory) {
    public Memory() {
        this(new ArrayList<>());
    }

    String display() {
        return IntStream.range(0, memory().size())
                .mapToObj(index -> new Tuple<>(index, memory().get(index)))
                .map(tuple -> tuple.mapRight(Long::toHexString))
                .map(tuple -> tuple.left() + ") " + tuple.right())
                .collect(Collectors.joining("\n"));
    }

    public Optional<Long> get(int programCounter) {
        return programCounter < memory.size()
                ? Optional.of(memory.get(programCounter))
                : Optional.empty();
    }

    public Memory set(long index, long value) {
        final var copy = new ArrayList<>(memory);
        while (!(index < copy.size())) copy.add(0L);

        copy.set((int) index, value);
        return new Memory(copy);
    }
}