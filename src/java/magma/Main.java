package magma;

import java.util.*;
import java.util.function.Function;

import static magma.Operation.*;

public class Main {
    public static final int MEMORY_SIZE = 64;
    public static final int OP_CODE_SIZE = 8;
    public static final int INT = MEMORY_SIZE - OP_CODE_SIZE;
    public static final int ADDRESS_OR_VALUE_LENGTH = INT;

    public static void main(String[] args) {
        final var withSum = new State().defineData("sum", 1L);

        final var instructions = block(withSum, state -> {
            return state.defineData("a", 1L, _ -> List.of(LoadValue.of(new Value(100))))
                    .defineData("b", 1L, _ -> List.of(LoadValue.of(new Value(200))))
                    .assignAsState("sum", stack -> List.of(
                            LoadDirect.of(new DataAddress(stack.resolveAddress("a"))),
                            AddDirect.of(new DataAddress(stack.resolveAddress("b")))));
        });

        final var totalInstructions = new ArrayList<>(instructions.instructions());
        totalInstructions.add(Halt.empty());

        final var adjusted = totalInstructions.stream()
                .map(instruction -> instruction.offsetAddress(3).offsetData(totalInstructions.size()))
                .map(Instruction::toBinary)
                .toList();

        final var assembled = new ArrayList<>(set(2, JumpValue.of(new DataAddress(0)).toBinary()));
        for (int i = 0; i < adjusted.size(); i++) {
            assembled.addAll(set(3 + i, adjusted.get(i)));
        }
        assembled.addAll(set(2, JumpValue.of(new DataAddress(3)).toBinary()));

        final var memory = Collections.singletonList(InputDirect.of(new DataAddress(1)).toBinary());
        final var run = run(new EmulatorState(memory, new Port(assembled)));

        var joiner = new StringJoiner("\n");
        List<Long> longs = run.getMemory();
        for (int i = 0; i < longs.size(); i++) {
            long value = longs.get(i);
            final var result = decode(value);
            joiner.add(Long.toHexString(i) + ": " + result);
        }

        System.out.println(joiner);
    }

    private static State block(State state, Function<State, State> mapper) {
        final var entered = state.enter();
        final var applied = mapper.apply(entered);
        return applied.exit();
    }

    private static List<Long> set(int address, long value) {
        return List.of(
                InputDirect.of(new DataAddress(address)).toBinary(),
                value
        );
    }

    private static EmulatorState run(EmulatorState state) {
        var current = state;
        while (true) {
            final var cycled = cycle(current);
            if (cycled.isPresent()) {
                current = cycled.get();
            } else {
                return current;
            }
        }
    }

    private static Optional<EmulatorState> cycle(EmulatorState state) {
        return state.current().flatMap(instruction -> {
            final var result = decode(instruction);

            final var next = state.next();
            final var operation = result.operation();
            final var addressOrValue = result.addressOrValue();

            return switch (operation) {
                case Nothing -> Optional.of(next);
                case InputDirect -> Optional.of(next.inputDirect(addressOrValue));
                case JumpValue -> Optional.of(next.jumpValue(addressOrValue));
                case Halt -> Optional.empty();
                case LoadDirect -> Optional.of(next.loadDirect(addressOrValue));
                case AddValue -> Optional.of(next.addValue(addressOrValue));
                case StoreDirect -> Optional.of(next.storeDirect(addressOrValue));
                case LoadValue -> Optional.of(next.loadValue(addressOrValue));
                case LoadIndirect -> Optional.of(next.loadIndirect(addressOrValue));
                case StoreIndirect -> Optional.of(next.storeIndirect(addressOrValue));
                case SubtractValue -> Optional.of(next.subtractValue(addressOrValue));
                case AddDirect -> Optional.of(next.addDirect(addressOrValue));
            };
        });
    }

    private static Instruction decode(Long instruction) {
        final var opCode = (byte) (instruction >> ADDRESS_OR_VALUE_LENGTH);
        final var operation = apply(opCode).orElse(Nothing);
        final var addressOrValue = instruction & ((1L << ADDRESS_OR_VALUE_LENGTH) - 1);
        return operation.of(new DataAddress(addressOrValue));
    }
}
