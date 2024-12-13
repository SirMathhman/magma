package magma;

public enum Operation {
    InDirect, JumpValue, NoOp, LoadValue, StoreIndirect;

    public int of(int addressOrValue) {
        return (ordinal() << 24) + addressOrValue;
    }

    public int empty() {
        return of(0);
    }
}
