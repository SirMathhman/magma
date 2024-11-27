package magma;

import java.util.stream.IntStream;

enum OpCode {
    Nothing,
    InAddress,
    JumpValue,
    Halt,
    OutValue;

    public int of(int addressOrValue) {
        final var opCode = IntStream.range(0, values().length)
                .filter(index -> values()[index].equals(this))
                .findFirst()
                .orElse(0);

        return (opCode << 24) + addressOrValue;
    }

    public int empty() {
        return of(0);
    }
}
