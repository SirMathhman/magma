package magma;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

enum OpCode {
    Nothing,
    InAddress,
    JumpToValue,
    Halt,
    OutValue, LoadFromAddress, AddFromAddress;

    private static final int maxLength;

    public static String padLeft(OpCode code) {
        final var codeString = code.toString();
        return " ".repeat(maxLength - codeString.length()) + codeString;
    }

    static {
        maxLength = Arrays.stream(OpCode.values())
                .map(Objects::toString)
                .mapToInt(String::length)
                .max()
                .orElse(0);
    }

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
