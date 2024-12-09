package magma;

public final class Instruction {
    private final Operation operation;
    private final Operand operand;

    public Instruction(Operation operation, Operand operand) {
        this.operation = operation;
        this.operand = operand;
    }

    public Instruction(Operation operation) {
        this(operation, new Address(0));
    }

    long toBinary() {
        final var shiftedOpCode = (long) operation().ordinal() << Main.ADDRESS_OR_VALUE_LENGTH;
        return shiftedOpCode + addressOrValue();
    }

    @Override
    public String toString() {
        return operation + " " + Long.toHexString(operand.compute());
    }

    public Operation operation() {
        return operation;
    }

    public long addressOrValue() {
        return operand.compute();
    }

    public Instruction offset(int offset) {
        return new Instruction(operation, operand.offset(offset));
    }
}