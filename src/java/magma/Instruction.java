package magma;

public record Instruction(Operation operation, int addressOrValue) {
    static Instruction decode(int instruction) {
        final var opCode = instruction >> 24;
        final var addressOrValue = instruction & 0x00FFFFFF;

        final var operation = Operation.values()[opCode];
        return new Instruction(operation, addressOrValue);
    }
}