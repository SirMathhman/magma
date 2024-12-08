package magma;

import java.util.*;
import java.util.stream.Collectors;

import static magma.Main.Operation.*;

public class Main {
    public static final int MEMORY_SIZE = 64;
    public static final int OP_CODE_SIZE = 8;
    public static final int INT = MEMORY_SIZE - OP_CODE_SIZE;
    public static final int ADDRESS_OR_VALUE_LENGTH = INT;

    public static void main(String[] args) {
        var instructions = List.of(
                instruct(LoadDirect, 3),
                instruct(AddValue, 1),
                instruct(StoreDirect, 3),
                instruct(LoadValue, 100),
                instruct(StoreIndirect, 3),
                instruct(Halt)
        );

        final var assembled = new ArrayList<Long>();
        assembled.addAll(set(2, instruct(JumpValue, 0)));
        assembled.addAll(set(3, 4L + instructions.size()));
        assembled.addAll(set(4, 0L));

        for (int i = 0; i < instructions.size(); i++) {
            final var instruction = instructions.get(i);
            assembled.addAll(set(i + 5, instruction));
        }

        assembled.addAll(set(2, instruct(JumpValue, 5)));

        final var memory = Collections.singletonList(instruct(InputDirect, 1));
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

    private static List<Long> set(int address, long value) {
        return List.of(
                instruct(InputDirect, address),
                value
        );
    }

    private static long instruct(Operation operation) {
        return instruct(operation, 0);
    }

    private static long instruct(Operation operation, int addressOrValue) {
        final var shiftedOpCode = (long) operation.ordinal() << ADDRESS_OR_VALUE_LENGTH;
        return shiftedOpCode + addressOrValue;
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
            return switch (result.operation()) {
                case Nothing -> Optional.of(next);
                case InputDirect -> Optional.of(next.inputDirect(result.addressOrValue()));
                case JumpValue -> Optional.of(next.jumpValue(result.addressOrValue()));
                case Halt -> Optional.empty();
                case LoadDirect -> Optional.of(next.loadDirect(result.addressOrValue()));
                case AddValue -> Optional.of(next.addValue(result.addressOrValue()));
                case StoreDirect -> Optional.of(next.storeDirect(result.addressOrValue()));
                case LoadValue -> Optional.of(next.loadValue(result.addressOrValue()));
                case StoreIndirect -> Optional.of(next.storeIndirect(result.addressOrValue()));
            };
        });
    }

    private static Instruction decode(Long instruction) {
        final var opCode = (byte) (instruction >> ADDRESS_OR_VALUE_LENGTH);
        final var operation = apply(opCode).orElse(Nothing);
        final var addressOrValue = instruction & ((1L << ADDRESS_OR_VALUE_LENGTH) - 1);
        Instruction result = new Instruction(operation, addressOrValue);
        return result;
    }

    enum Operation {
        Nothing,
        InputDirect,
        JumpValue,
        Halt, LoadDirect, AddValue, StoreDirect, LoadValue, StoreIndirect;

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

    private record Instruction(Operation operation, long addressOrValue) {
        @Override
        public String toString() {
            return operation + " " + Long.toHexString(addressOrValue);
        }
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
            memory.set((int) address, accumulator);
            return this;
        }

        public State loadValue(long value) {
            accumulator = value;
            return this;
        }

        public State storeIndirect(long address) {
            final var resolved = memory.get((int) address);
            set(Math.toIntExact(resolved), accumulator);
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
