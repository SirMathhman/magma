package magma;

public record DataAddress(long address) implements Operand {
    @Override
    public long compute() {
        return address;
    }

    @Override
    public Operand offsetData(int offset) {
        return new DataAddress(address + offset);
    }

    @Override
    public Operand offsetAddress(int offset) {
        return new DataAddress(address + offset);
    }
}