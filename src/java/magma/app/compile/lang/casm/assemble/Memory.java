package magma.app.compile.lang.casm.assemble;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record Memory(List<Integer> memory) {
    public String display() {
        return IntStream.range(0, memory.size())
                .mapToObj(this::formatMemoryCell)
                .collect(Collectors.joining("", "[", "\n\t]"));
    }

    private String formatMemoryCell(int index) {
        final var indexAsHex = Integer.toHexString(index);
        final var indexAsHexPadded = " ".repeat(8 - indexAsHex.length()) + indexAsHex;
        final var instruction = Instruction.fromValue(memory.get(index));
        return "\n\t\t%s - %s".formatted(indexAsHexPadded, instruction);
    }

    public Option<Integer> resolve(int programCounter) {
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

    public Memory storeIndirectly(int address, int accumulator) {
        final var realized = memory.get(address);
        return set(realized, accumulator);
    }
}
