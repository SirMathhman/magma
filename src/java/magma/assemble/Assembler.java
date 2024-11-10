package magma.assemble;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.ApplicationError;
import magma.app.ThrowableError;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.CASMLang;
import magma.java.JavaList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Assembler {
    public static final Path ROOT = Paths.get(".", "src", "magma");
    public static final Path TARGET = ROOT.resolve("main.casm");

    public static final int INPUT_AND_LOAD = 0x00;
    public static final int INPUT_AND_STORE = 0x10;
    public static final int LOAD = 0x01;
    public static final int STORE = 0x02;
    public static final int OUT = 0x03;
    public static final int ADD_ADDRESS = 4;
    public static final int ADD_VALUE = 0x14;
    public static final int SUB = 0x05;
    public static final int INCREMENT = 0x06;
    public static final int DEC = 0x07;
    public static final int TAC = 0x08;
    public static final int JUMP_ADDRESS = 0x09;
    public static final int HALT = 0x0A;
    public static final int SFT = 0x0B;
    public static final int SHIFT_LEFT = 0x0C;
    public static final int SHIFT_RIGHT = 0x0D;
    public static final int TS = 0x0E;
    public static final int CAS = 0x0F;
    public static final int BYTES_PER_LONG = 8;
    public static final int STACK_POINTER_ADDRESS = 4;
    private static final int PUSH = 0x11;
    private static final int POP = 0x12;
    private static final int NO_OPERATION = 0x13;

    public static void main(String[] args) {
        readAndExecute().ifPresent(error -> System.err.println(error.format(0, 0)));
    }

    static Option<ApplicationError> readAndExecute() {
        return readSafe(TARGET)
                .mapErr(ThrowableError::new)
                .mapErr(ApplicationError::new)
                .match(Assembler::assembleAndExecute, Some::new);
    }

    private static Option<ApplicationError> assembleAndExecute(String input) {
        return assemble(input).mapErr(ApplicationError::new).match(list -> {
            execute(list);
            return new None<>();
        }, Some::new);
    }

    private static void execute(Deque<Long> input) {
        System.out.println("Memory footprint: " + (input.size() * BYTES_PER_LONG) + " bytes");

        final var memory = new ArrayList<Long>();
        compute(memory, input);
        System.out.println();
        System.out.println("Final Memory State: " + formatHexList(memory, ", "));
    }

    private static void compute(List<Long> memory, Deque<Long> input) {
        memory.add(new Instruction(INPUT_AND_STORE, new Constant(1L)).evaluate());

        long accumulator = 0;  // Holds current value for operations
        int programCounter = 0;

        var state = new State(memory, input, programCounter, accumulator);
        while (programCounter < memory.size()) {
            final long instructionUnsigned = memory.get(programCounter);

            int opcode = (int) ((instructionUnsigned >> 56) & 0xFF);
            long addressOrValue = instructionUnsigned & 0x00FFFFFFFFFFFFFFL;

            final var state1 = state.setProgramCounter(state.programCounter + 1);
            final var option = switchOk(state1, opcode, addressOrValue);
            if (option.isEmpty()) return;

            state = option.orElse(state);
        }
    }

    private static Option<State> switchOk(State state, int opcode, long addressOrValue) {
        switch (opcode) {
            case HALT:
                return new None<>();
            case NO_OPERATION:
                return new Some<>(state);
            case PUSH:
                return new Some<>(push(state, addressOrValue));
            case POP:
                return new Some<>(state.setAccumulator(pop(state.memory())));
            case INPUT_AND_LOAD:
                return new Some<>(state.setAccumulator(inputAndLoad(state.input())));
            case INPUT_AND_STORE:
                return new Some<>(inputAndStore(state, state.memory(), state.input(), addressOrValue));
            case LOAD:
                return new Some<>(state.setAccumulator(load(state.memory(), addressOrValue)));
            case STORE:
                return new Some<>(store(state, state.memory(), addressOrValue, state.accumulator()));
            case OUT:
                return new Some<>(out(state, state.accumulator()));
            case ADD_ADDRESS:
                return new Some<>(state.setAccumulator(add(state.memory(), addressOrValue, state.accumulator())));
            case ADD_VALUE:
                return new Some<>(state.setAccumulator(addValue(state.accumulator(), addressOrValue)));
            case SUB:
                return new Some<>(state.setAccumulator(sub(state.memory(), addressOrValue, state.accumulator())));
            case INCREMENT:
                return new Some<>(increment(state, state.memory(), addressOrValue));
            case DEC:
                return new Some<>(decrement(state, state.memory(), addressOrValue));
            case TAC:
                return new Some<>(state.setProgramCounter(testConditional(state.accumulator(), state.programCounter(), (int) addressOrValue)));
            case JUMP_ADDRESS:
                return new Some<>(state.setProgramCounter(jump((int) addressOrValue)));
            case SHIFT_LEFT:
                return new Some<>(state.setAccumulator(shiftLeft(state.accumulator(), addressOrValue)));
            case SHIFT_RIGHT:
                return new Some<>(state.setAccumulator(shiftRight(state.accumulator(), addressOrValue)));
            case TS:
                return new Some<>(state.setProgramCounter(testAndSet(state.memory(), addressOrValue, state.programCounter())));
            case CAS:
                return new Some<>(compareAndSwap(state, state.memory(), addressOrValue));
            default:
                System.err.println("Unknown opcode: " + opcode);
                return new None<>();
        }
    }

    private static State compareAndSwap(State state, List<Long> memory, long addressOrValue) {
        long oldValue = memory.get((int) addressOrValue);
        long compareValue = (addressOrValue >> 32) & 0xFFFFFFFFL;
        long newValue = addressOrValue & 0xFFFFFFFFL;
        if (oldValue == compareValue) {
            memory.set((int) addressOrValue, newValue);
        }
        return state;
    }

    private static int testAndSet(List<Long> memory, long addressOrValue, int programCounter) {
        if (addressOrValue < memory.size()) {
            if (memory.get((int) addressOrValue) == 0) {
                memory.set((int) addressOrValue, 1L);
            } else {
                programCounter--;  // Retry this instruction if lock isn't available
            }
        }
        return programCounter;
    }

    private static long shiftRight(long accumulator, long addressOrValue) {
        accumulator >>= addressOrValue;
        return accumulator;
    }

    private static long shiftLeft(long accumulator, long addressOrValue) {
        accumulator <<= addressOrValue;
        return accumulator;
    }

    private static int jump(int addressOrValue) {
        int programCounter;
        programCounter = addressOrValue;
        return programCounter;
    }

    private static int testConditional(long accumulator, int programCounter, int addressOrValue) {
        if (accumulator < 0) {
            programCounter = addressOrValue;
        }
        return programCounter;
    }

    private static State decrement(State state, List<Long> memory, long addressOrValue) {
        if (addressOrValue < memory.size()) {
            memory.set((int) addressOrValue, memory.get((int) addressOrValue) - 1);
        }
        return state;
    }

    private static State increment(State state, List<Long> memory, long addressOrValue) {
        if (addressOrValue < memory.size()) {
            final var cast = (int) addressOrValue;
            memory.set(cast, memory.get(cast) + 1);
        }
        return state;
    }

    private static long sub(List<Long> memory, long addressOrValue, long accumulator) {
        if (addressOrValue < memory.size()) {
            accumulator -= memory.get((int) addressOrValue);
        } else {
            System.err.println("Address out of bounds.");
        }
        return accumulator;
    }

    private static long addValue(long accumulator, long addressOrValue) {
        accumulator += addressOrValue;
        return accumulator;
    }

    private static long add(List<Long> memory, long addressOrValue, long accumulator) {
        if (addressOrValue < memory.size()) {
            accumulator = addValue(accumulator, memory.get((int) addressOrValue));
        } else {
            System.err.println("Address out of bounds.");
        }
        return accumulator;
    }

    private static State out(State state, long accumulator) {
        System.out.print(accumulator);
        return state;
    }

    private static State store(State state, List<Long> memory, long addressOrValue, long accumulator) {
        if (addressOrValue < memory.size()) {
            memory.set((int) addressOrValue, accumulator);
        } else {
            System.err.println("Address out of bounds.");
        }
        return state;
    }

    private static long load(List<Long> memory, long addressOrValue) {
        long accumulator;
        accumulator = addressOrValue < memory.size() ? memory.get((int) addressOrValue) : 0;
        return accumulator;
    }

    private static State inputAndStore(State state, List<Long> memory, Deque<Long> input, long addressOrValue) {
        if (input.isEmpty()) {
            throw new RuntimeException("Input queue is empty.");
        } else {
            final var polled = input.poll();
            set(memory, addressOrValue, polled);
        }
        return state;
    }

    private static long inputAndLoad(Deque<Long> input) {
        long accumulator;
        if (input.isEmpty()) {
            throw new RuntimeException("Input queue is empty.");
        } else {
            accumulator = input.poll();
        }
        return accumulator;
    }

    private static long pop(List<Long> memory) {
        long accumulator;
        set(memory, STACK_POINTER_ADDRESS, Math.max(memory.get(STACK_POINTER_ADDRESS) - 1, 0));
        accumulator = memory.get((int) memory.get(STACK_POINTER_ADDRESS).longValue());
        return accumulator;
    }

    private static State push(State state, long addressOrValue) {
        set(state.memory, state.memory.get(STACK_POINTER_ADDRESS), addressOrValue);
        set(state.memory, STACK_POINTER_ADDRESS, state.memory.get(STACK_POINTER_ADDRESS) + 1);
        return state;
    }

    private static void set(List<Long> memory, long address, long value) {
        while (!(address < memory.size())) {
            memory.add(0L);
        }

        memory.set((int) address, value);
    }

    private static String formatHexList(List<Long> list, String delimiter) {
        return list.stream()
                .map(value -> Long.toString(value, 16))
                .collect(Collectors.joining(delimiter, "[", "]"));
    }

    private static Result<Deque<Long>, CompileError> assemble(String content) {
        return CASMLang.createRootRule()
                .parse(content)
                .mapValue(Assembler::parse);
    }

    private static Deque<Long> parse(Node root) {
        return new LinkedList<>();
    }

    static Result<String, IOException> readSafe(Path path) {
        try {
            return new Ok<>(Files.readString(path));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    interface Evaluatable {
        long evaluate();
    }

    private record Instructions(JavaList<Long> list) {
        public Instructions() {
            this(new JavaList<>());
        }

        private Deque<Long> toDeque() {
            return new LinkedList<>(list.list());
        }

        private Instructions set(long address, long value) {
            return new Instructions(list()
                    .add(new Instruction(INPUT_AND_STORE, new Constant(address)).evaluate())
                    .add(value));
        }
    }

    private record Constant(long addressOrValue) implements Evaluatable {
        @Override
        public long evaluate() {
            return addressOrValue;
        }
    }

    private record Instruction(int opcode, Evaluatable evaluatable) {
        private long evaluate() {
            if (opcode() < 0x00 || opcode() > 0xFF) {
                throw new IllegalArgumentException("Opcode must be an 8-bit value (0x00 to 0xFF).");
            }
            if (evaluatable().evaluate() < 0 || evaluatable().evaluate() > 0x00FFFFFFFFFFFFFFL) {
                throw new IllegalArgumentException("Address/Value must be a 56-bit value (0x00 to 0x00FFFFFFFFFFFFFF).");
            }

            return ((long) opcode() << 56) | evaluatable().evaluate();
        }
    }

    private static class State {
        private final List<Long> memory;
        private final Deque<Long> input;
        private int programCounter;
        private long accumulator;

        private State(List<Long> memory, Deque<Long> input, int programCounter, long accumulator) {
            this.memory = memory;
            this.input = input;
            this.programCounter = programCounter;
            this.accumulator = accumulator;
        }

        public List<Long> memory() {
            return memory;
        }

        public Deque<Long> input() {
            return input;
        }

        public int programCounter() {
            return programCounter;
        }

        public long accumulator() {
            return accumulator;
        }

        public State setProgramCounter(int programCounter) {
            this.programCounter = programCounter;
            return this;
        }

        public State setAccumulator(long accumulator) {
            this.accumulator = accumulator;
            return this;
        }
    }
}
