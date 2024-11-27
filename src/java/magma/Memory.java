package magma;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record Memory(List<Integer> memory) {
    public String display() {
        return memory.stream()
                .map(Instruction::fromValue)
                .map(Object::toString)
                .map(value -> "\n\t\t" + value)
                .collect(Collectors.joining(",", "[", "\n\t]"));
    }

    public Option<Integer> get(int programCounter) {
        return programCounter < memory.size()
                ? new Some<>(memory.get(programCounter))
                : new None<>();
    }

    Memory set(int address, int value) {
        final var copy = new ArrayList<>(memory());
        while (address >= copy.size()) {
            copy.add(0);
        }
        copy.set(address, value);
        return new Memory(copy);
    }
}
