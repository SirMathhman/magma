package magma;

import java.util.Optional;

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

    public String display() {
        return operation + " " + Integer.toHexString(addressOrValue);
    }
}