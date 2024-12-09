package magma;

public record Value(int value) implements Operand {
    @Override
    public long compute() {
        return value;
    }

    @Override
    public Operand offsetData(int offset) {
        return new Value(value);
    }
}
