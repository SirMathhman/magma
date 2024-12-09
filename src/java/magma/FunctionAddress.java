package magma;

public record FunctionAddress(long address) implements Operand {
    @Override
    public long compute() {
        return address;
    }

    @Override
    public Operand offsetData(int offset) {
        return new FunctionAddress(address);
    }

    @Override
    public Operand offsetAddress(int offset) {
        return new FunctionAddress(address + offset);
    }
}