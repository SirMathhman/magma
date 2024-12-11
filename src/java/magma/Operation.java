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

    static int encode(Node node) {
        final var shiftedOrdinal = node.ordinal().orElse(0) << 24;
        return shiftedOrdinal + node.addressOrValue().orElse(0);
    }
}
