package magma;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Operation {
    Nothing,
    InputDirect,
    JumpValue,
    Halt,
    AddValue,
    StoreDirect,
    LoadValue,
    LoadDirect,
    LoadIndirect,
    StoreIndirect,
    SubtractValue,
    AddDirect;

    public static final Map<Byte, Operation> OP_CODE_TO_OPERATION = new HashMap<>();

    static {
        Operation[] values = values();
        for (int i = 0; i < values.length; i++) {
            Operation value = values[i];
            OP_CODE_TO_OPERATION.put((byte) i, value);
        }
    }

    public static Optional<Operation> apply(byte opCode) {
        return Optional.of(OP_CODE_TO_OPERATION.get(opCode));
    }
}
