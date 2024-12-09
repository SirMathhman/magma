package magma;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static magma.Main.Operation.*;

public class Main {
    public static final int MEMORY_SIZE = 64;
    public static final int OP_CODE_SIZE = 8;
    public static final int INT = MEMORY_SIZE - OP_CODE_SIZE;
    public static final int ADDRESS_OR_VALUE_LENGTH = INT;

    public static void main(String[] args) {
        var instructions = Stream.of(
                List.of(instruct(Halt))
        ).flatMap(Collection::stream).toList();

        final var assembled = new ArrayList<>(set(2, instruct(JumpValue, 0)));

        for (int i = 0; i < instructions.size(); i++) {
            final var instruction = instructions.get(i);
            assembled.addAll(set(i + 3, instruction));
        }

        assembled.addAll(set(2, instruct(JumpValue, 3)));

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

    private static List<Long> loadOffset(int offset) {
        return Stream.of(moveTo(offset),
                        List.of(instruct(LoadIndirect, 3)),
                        moveTo(-offset),
                        List.of(instruct(StoreDirect, 4)))
                .flatMap(Collection::stream).toList();
    }

    private static List<Long> insertAt(int offset, List<Long> loader) {
        return Stream.of(loader,
                        moveTo(offset), List.of(instruct(StoreIndirect, 3)),
                        moveTo(-offset))
                .flatMap(Collection::stream).toList();
    }

    private static List<Long> moveTo(int offset) {
        if (offset > 0) {
            return moveByInstruction(instruct(AddValue, offset));
        } else {
            return moveByInstruction(instruct(SubtractValue, -offset));
        }
    }

    private static List<Long> moveByInstruction(long instruction) {
        return List.of(
                instruct(StoreDirect, 4),
                instruct(LoadDirect, 3),
                instruction,
                instruct(StoreDirect, 3),
                instruct(LoadDirect, 4)
        );
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
                case SubtractValue -> Optional.of(next.subtractValue(result.addressOrValue));
                case AddDirect -> Optional.of(next.addDirect(result.addressOrValue));
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
