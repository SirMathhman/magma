package magma;

public record Instruction(OpCode opCode, int addressOrValue) {
    @Override
    public String toString() {
        return opCode + " " + addressOrValue;
    }

    static Instruction fromValue(Integer value) {
        final var opCode = OpCode.values()[(value >> 24) & 0xFF];
        final var addressOrValue = value & 0x00FFFFFF;
        return new Instruction(opCode, addressOrValue);
    }
}