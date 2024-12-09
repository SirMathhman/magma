package magma;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        final var totalInstructions = new ArrayList<>(instructions.instructions);
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
        List<Long> longs = run.memory;
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

    record State(Stack stack, List<Instruction> instructions) {
        public State() {
            this(new Stack(), new ArrayList<>());
        }

        public State enter() {
            return new State(stack.enter(), instructions);
        }

        public State exit() {
            return new State(stack.exit(), instructions);
        }

        public State defineData(String name, long size, Function<Stack, List<Instruction>> loader) {
            final var withA = stack.define(name, size);
            final var instructions = assign(name, loader);
            return new State(withA, instructions);
        }

        public State assignAsState(String name, Function<Stack, List<Instruction>> loader) {
            final var instructions = assign(name, loader);
            return new State(stack, instructions);
        }

        private List<Instruction> assign(String name, Function<Stack, List<Instruction>> loader) {
            final var instructions = Stream.of(loader.apply(stack), List.of(StoreDirect.of(new DataAddress(stack.resolveAddress(name)))))
                    .flatMap(Collection::stream)
                    .toList();

            final var copy = new ArrayList<>(this.instructions);
            copy.addAll(instructions);
            return copy;
        }

        public State defineData(String name, long size) {
            return new State(stack.define(name, size), instructions);
        }
    }

    private static class EmulatorState {
        private final List<Long> memory;
        private final Port port;
        private long accumulator;
        private int programCounter;

        public EmulatorState(List<Long> memory, Port port) {
            this.memory = new ArrayList<>(memory);
            this.port = port;
            this.programCounter = 0;
            this.accumulator = 0;
        }

        @Override
        public String toString() {
            final var joinedMemory = memory.stream()
                    .map(Long::toHexString)
                    .collect(Collectors.joining(", ", "[", "]"));

            return "State{" +
                    "memory=" + joinedMemory +
                    ", programCounter=" + programCounter +
                    '}';
        }

        public EmulatorState next() {
            programCounter++;
            return this;
        }

        public Optional<Long> current() {
            if (programCounter < memory.size()) {
                return Optional.of(memory.get(programCounter));
            } else {
                return Optional.empty();
            }
        }

        public EmulatorState inputDirect(long address) {
            final var read = port.read();

            if (read.isEmpty()) return this;
            final var next = read.orElse(0L);

            set((int) address, next);
            return this;
        }

        private void set(int address, Long value) {
            while (!(address < memory.size())) {
                memory.add(0L);
            }
            memory.set(address, value);
        }

        public EmulatorState jumpValue(long address) {
            if (address < memory.size()) {
                programCounter = (int) address;
            }
            return this;
        }

        public EmulatorState loadDirect(long address) {
            accumulator = memory.get((int) address);
            return this;
        }

        public EmulatorState addValue(long value) {
            accumulator += value;
            return this;
        }

        public EmulatorState storeDirect(long address) {
            set((int) address, accumulator);
            return this;
        }

        public EmulatorState loadValue(long value) {
            accumulator = value;
            return this;
        }

        public EmulatorState storeIndirect(long address) {
            if (address < memory.size()) {
                final var resolved = memory.get((int) address);
                set(Math.toIntExact(resolved), accumulator);
            }
            return this;
        }

        public EmulatorState subtractValue(long value) {
            accumulator -= value;
            return this;
        }

        public EmulatorState loadIndirect(long address) {
            final var next = memory.get((int) address);
            accumulator = memory.get(Math.toIntExact(next));
            return this;
        }

        public EmulatorState addDirect(long address) {
            accumulator += memory.get((int) address);
            return this;
        }
    }

    private static class Port {
        private final List<Long> buffer;
        private int counter = 0;

        public Port(List<Long> buffer) {
            this.buffer = buffer;
        }

        public Optional<Long> read() {
            if (counter >= buffer.size()) return Optional.empty();

            final var value = buffer.get(counter);
            counter++;
            return Optional.of(value);
        }
    }

}
