package magma;

public record Instruction(Operation operation, int addressOrValue) {
    public static Instruction decode(int value) {
        final var opCode = value >> 24;
        final var operation = Operation.values()[opCode];

        final var addressOrValue = value & 0x0FFFFFF;
        return new Instruction(operation, addressOrValue);
    }

    @Override
    public String toString() {
        return operation + " " + Integer.toHexString(addressOrValue);
    }
}
