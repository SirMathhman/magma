package magma;

import java.util.*;
import java.util.stream.Collectors;

import static magma.Operation.*;

public class Main {
    public static final int MEMORY_SIZE = 64;
    public static final int OP_CODE_SIZE = 8;
    public static final int INT = MEMORY_SIZE - OP_CODE_SIZE;
    public static final int ADDRESS_OR_VALUE_LENGTH = INT;

    public static void main(String[] args) {
        var frames = new ArrayList<List<Tuple<String, Long>>>(List.of(new ArrayList<>()));

        frames.getLast().add(new Tuple<>("x", 1L));
        var instructions = new ArrayList<>(List.of(
                LoadValue.of(new Value(100)),
                StoreDirect.of(new DataAddress(resolveAddress(frames.getLast(), "x")))
        ));

        frames.getLast().add(new Tuple<>("y", 1L));
        instructions.addAll(List.of(
                LoadValue.of(new Value(200)),
                StoreDirect.of(new DataAddress(resolveAddress(frames.getLast(), "y")))
        ));

        frames.getLast().add(new Tuple<>("z", 1L));
        instructions.addAll(List.of(
                LoadValue.of(new DataAddress(resolveAddress(frames.getLast(), "x"))),
                AddDirect.of(new DataAddress(resolveAddress(frames.getLast(), "y"))),
                StoreDirect.of(new DataAddress(resolveAddress(frames.getLast(), "z")))
        ));

        instructions.add(Halt.empty());
        final var adjusted = instructions.stream()
                .map(instruction -> instruction.offsetAddress(3).offsetData(instructions.size()))
                .map(Instruction::toBinary)
                .toList();

        final var assembled = new ArrayList<>(set(2, JumpValue.of(new DataAddress(0)).toBinary()));
        for (int i = 0; i < adjusted.size(); i++) {
            assembled.addAll(set(3 + i, adjusted.get(i)));
        }
        assembled.addAll(set(2, JumpValue.of(new DataAddress(3)).toBinary()));

        final var memory = Collections.singletonList(InputDirect.of(new DataAddress(1)).toBinary());
        final var run = run(new State(memory, new Port(assembled)));

        var joiner = new StringJoiner("\n");
        List<Long> longs = run.memory;
        for (int i = 0; i < longs.size(); i++) {
            long value = longs.get(i);
            final var result = decode(value);
            joiner.add(Long.toHexString(i) + ": " + result);
        }

        System.out.println(joiner);
    }

    private static long resolveAddress(List<Tuple<String, Long>> frame, String name) {
        var sum = 0;
        for (Tuple<String, Long> entry : frame) {
            final var left = entry.left();
            if (left.equals(name)) {
                return sum;
            } else {
                sum += entry.right();
            }
        }

        return sum;
    }

    private static List<Long> set(int address, long value) {
        return List.of(
                InputDirect.of(new DataAddress(address)).toBinary(),
                value
        );
    }

    private static State run(State state) {
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

    private static Optional<State> cycle(State state) {
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

    private static class State {
        private final List<Long> memory;
        private final Port port;
        private long accumulator;
        private int programCounter;

        public State(List<Long> memory, Port port) {
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

        public State next() {
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

        public State inputDirect(long address) {
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

        public State jumpValue(long address) {
            if (address < memory.size()) {
                programCounter = (int) address;
            }
            return this;
        }

        public State loadDirect(long address) {
            accumulator = memory.get((int) address);
            return this;
        }

        public State addValue(long value) {
            accumulator += value;
            return this;
        }

        public State storeDirect(long address) {
            set((int) address, accumulator);
            return this;
        }

        public State loadValue(long value) {
            accumulator = value;
            return this;
        }

        public State storeIndirect(long address) {
            if (address < memory.size()) {
                final var resolved = memory.get((int) address);
                set(Math.toIntExact(resolved), accumulator);
            }
            return this;
        }

        public State subtractValue(long value) {
            accumulator -= value;
            return this;
        }

        public State loadIndirect(long address) {
            final var next = memory.get((int) address);
            accumulator = memory.get(Math.toIntExact(next));
            return this;
        }

        public State addDirect(long address) {
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
