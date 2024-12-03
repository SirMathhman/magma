package magma.app.compile.lang.casm.assemble;

public record Instruction(Operator operator, int addressOrValue) {
    static Instruction fromValue(Integer value) {
        final var opCode = (value >> 24) & 0xFF;
        final var values = Operator.values();
        if (opCode >= values.length) {
            throw new IllegalArgumentException("Unknown op-code: " + opCode);
        }

        final var operator = values[opCode];
        final var addressOrValue = value & 0x00FFFFFF;
        return new Instruction(operator, addressOrValue);
    }

    @Override
    public String toString() {
        final var valueString = Integer.toHexString(addressOrValue);
        final var paddedValueString = " ".repeat(8 - valueString.length()) + valueString;
        return Operator.padLeft(operator) + " " + paddedValueString;
    }
}