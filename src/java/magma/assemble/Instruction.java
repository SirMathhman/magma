package magma.assemble;

public record Instruction(Operator operator, int addressOrValue) {
    @Override
    public String toString() {
        final var valueString = Integer.toHexString(addressOrValue);
        final var paddedValueString = " ".repeat(8 - valueString.length()) + valueString;
        return Operator.padLeft(operator) + " " + paddedValueString;
    }

    static Instruction fromValue(Integer value) {
        final var opCode = Operator.values()[(value >> 24) & 0xFF];
        final var addressOrValue = value & 0x00FFFFFF;
        return new Instruction(opCode, addressOrValue);
    }
}