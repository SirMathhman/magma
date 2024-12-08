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
        var input = new ArrayList<Long>(List.of(
                instruct(InputDirect, 2),
                instruct(JumpValue, 0),
                instruct(InputDirect, 3),
                instruct(Halt),
                instruct(InputDirect, 2),
                instruct(JumpValue, 3)
        ));

        final var memory = Collections.singletonList(instruct(InputDirect, 1));
        final var run = run(new State(memory, new Port(input)));

        var joiner = new StringJoiner(", ");
        for (long value : run.memory) {
            joiner.add(Long.toHexString(value));
        }

        System.out.println("[" + joiner + "]");
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
            final var opCode = (byte) (instruction >> ADDRESS_OR_VALUE_LENGTH);
            final var operation = apply(opCode).orElse(Nothing);
            final var addressOrValue = instruction & ((1L << ADDRESS_OR_VALUE_LENGTH) - 1);

            final var next = state.next();
            return switch (operation) {
                case Nothing -> Optional.of(next);
                case InputDirect -> Optional.of(next.inputDirect(addressOrValue));
                case JumpValue -> Optional.of(next.jumpValue(addressOrValue));
                case Halt -> Optional.empty();
            };
        });
    }

    enum Operation {
        Nothing,
        InputDirect,
        JumpValue,
        Halt;

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

    private static class State {
        private final List<Long> memory;
        private final Port port;
        private int programCounter;

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

        public State(List<Long> memory, Port port) {
            this(memory, port, 0);
        }

        public State(List<Long> memory, Port port, int programCounter) {
            this.memory = new ArrayList<>(memory);
            this.port = port;
            this.programCounter = programCounter;
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
