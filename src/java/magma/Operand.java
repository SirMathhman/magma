package magma;

public interface Operand {
    long compute();

    Operand offsetData(int offset);

    Operand offsetAddress(int offset);
}
