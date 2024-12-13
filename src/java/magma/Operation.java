package magma;

public enum Operation {
    InAndStore, Jump, NoOp;

    public int of(int addressOrValue) {
        return (ordinal() << 24) + addressOrValue;
    }

    public int empty() {
        return of(0);
    }
}
