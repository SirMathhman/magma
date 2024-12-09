package magma;

import java.util.Optional;

public enum Operation {
    InputDirect;

    static Optional<Operation> apply(int opCode) {
        final var values = values();
        if (opCode< values.length) {
            return Optional.of(values[opCode]);
        } else {
            return Optional.empty();
        }
    }

    int instruct(int addressOrValue) {
        return ordinal() << 56 + addressOrValue;
    }
}
