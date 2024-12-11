package magma;

public enum Operation {
    Nothing,
    InAndStore,
    Halt;

    int empty() {
        return of(0);
    }

    int of(int addressOrValue) {
        return (ordinal() << 24) + addressOrValue;
    }
}
