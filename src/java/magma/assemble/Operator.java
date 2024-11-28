package magma.assemble;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public enum Operator {
    Nothing,
    InAddress,
    OutFromValue,
    OutFromAccumulator,
    StoreDirectly,
    LoadFromValue,
    LoadFromAddress,
    AddFromAddress,
    AddFromValue,
    SubtractValue,
    Not,
    JumpByValue,
    JumpByAddress,
    JumpConditionByValue,
    Halt, StoreIndirectly;

    private static final int maxLength;

    static {
        maxLength = Arrays.stream(Operator.values())
                .map(Objects::toString)
                .mapToInt(String::length)
                .max()
                .orElse(0);
    }

    public static Option<Operator> find(int value) {
        final var array = Operator.values();
        if (value < array.length) {
            return new Some<>(array[value]);
        } else {
            return new None<>();
        }
    }

    public static String padLeft(Operator code) {
        final var codeString = code.toString();
        return " ".repeat(maxLength - codeString.length()) + codeString;
    }

    public int of(int addressOrValue) {
        final var opCode = computeOpCode();
        return (opCode << 24) + addressOrValue;
    }

    public int computeOpCode() {
        return IntStream.range(0, values().length)
                .filter(index -> values()[index].equals(this))
                .findFirst()
                .orElse(0);
    }

    public int empty() {
        return of(0);
    }
}
