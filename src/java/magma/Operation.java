package magma;

public enum Operation {
    Nothing,
    InStore,
    Halt,
    Jump,
    LoadValue,
    StoreIndirect,
    StoreDirect,
    LoadDirect,
    AddValue,
    SubtractValue,
    AddDirect,
    LoadIndirect, SubtractDirect, JumpCond;

    int empty() {
        return of(0);
    }

    int of(int addressOrValue) {
        return (ordinal() << 24) + addressOrValue;
    }
}
