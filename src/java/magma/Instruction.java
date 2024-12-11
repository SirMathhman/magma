package magma;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record Instruction(Operation operation, int addressOrValue) {
    static Optional<Instruction> decode(int instruction) {
        final var opCode = instruction >> 24;
        final var addressOrValue = instruction & 0x00FFFFFF;

        final var values = Operation.values();
        if (opCode >= 0 && opCode < values.length) {
            final var operation = values[opCode];
            return Optional.of(new Instruction(operation, addressOrValue));
        } else {
            return Optional.empty();
        }
    }

    static String displayEncodedInstruction(Tuple<Integer, Integer> tuple) {
        final var index = tuple.left();
        final var value = tuple.right();

        final var indexHex = Integer.toHexString(index);
        final var instruction = decode(value)
                .map(Instruction::display)
                .orElse(String.valueOf(value));

        return indexHex + ") " + instruction;
    }

    static String displayEncoded(List<Integer> instructions) {
        return IntStream.range(0, instructions.size())
                .mapToObj(index -> new Tuple<>(index, instructions.get(index)))
                .map(Instruction::displayEncodedInstruction)
                .collect(Collectors.joining("\n"));
    }

    public String display() {
        return operation + " " + Integer.toHexString(addressOrValue);
    }
}