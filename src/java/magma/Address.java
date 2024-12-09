package magma;

public record Address(long address) implements Operand {
    @Override
    public long compute() {
        return address;
    }

    @Override
    public Operand offset(int offset) {
        return new Address(address + offset);
    }
}