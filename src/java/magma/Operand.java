package magma;

public interface Operand {
    long compute();

    Operand offset(int offset);
}
