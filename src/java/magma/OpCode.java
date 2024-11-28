package magma;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

enum OpCode {
    Nothing,
    InAddress,
    JumpToValue,
    Halt,
    OutValue,
    OutToAccumulator,
    LoadFromAddress,
    AddFromAddress;

    private static final int maxLength;

    static {
        maxLength = Arrays.stream(OpCode.values())
                .map(Objects::toString)
                .mapToInt(String::length)
                .max()
                .orElse(0);
    }

    public static String padLeft(OpCode code) {
        final var codeString = code.toString();
        return " ".repeat(maxLength - codeString.length()) + codeString;
    }

    public int of(int addressOrValue) {
        final var opCode = computeOpCode();
        return (opCode << 24) + addressOrValue;
    }

    int computeOpCode() {
        return IntStream.range(0, values().length)
                .filter(index -> values()[index].equals(this))
                .findFirst()
                .orElse(0);
    }

    public int empty() {
        return of(0);
    }
}
