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

        while (programCounter < memory.size()) {
            final long instructionUnsigned = memory.get(programCounter);

            // Decode the instruction
            int opcode = (int) ((instructionUnsigned >> 56) & 0xFF);  // First 8 bits
            long addressOrValue = instructionUnsigned & 0x00FFFFFFFFFFFFFFL;  // Remaining 56 bits

            programCounter++;  // Move to next instruction by default

            // Execute based on opcode
            switch (opcode) {
                case NO_OPERATION:
                    break;
                case PUSH:
                    push(memory, addressOrValue);
                    break;
                case POP:
                    accumulator = pop(memory);
                    break;
                case INPUT_AND_LOAD:
                    accumulator = inputAndLoad(input);
                    break;
                case INPUT_AND_STORE:
                    inputAndStore(memory, input, addressOrValue);
                    break;
                case LOAD:
                    accumulator = load(memory, addressOrValue);
                    break;
                case STORE:
                    store(memory, addressOrValue, accumulator);
                    break;
                case OUT:
                    out(accumulator);
                    break;
                case ADD_ADDRESS:
                    accumulator = add(memory, addressOrValue, accumulator);
                    break;
                case ADD_VALUE:
                    accumulator = addValue(accumulator, addressOrValue);
                    break;
                case SUB:
                    accumulator = sub(memory, addressOrValue, accumulator);
                    break;
                case INCREMENT:
                    increment(memory, addressOrValue);
                    break;
                case DEC:
                    decrement(memory, addressOrValue);
                    break;
                case TAC:
                    programCounter = testConditional(accumulator, programCounter, (int) addressOrValue);
                    break;
                case JUMP_ADDRESS:
                    programCounter = jump((int) addressOrValue);
                    break;
                case HALT:
                    return;
                case SHIFT_LEFT:
                    accumulator = shiftLeft(accumulator, addressOrValue);
                    break;
                case SHIFT_RIGHT:
                    accumulator = shiftRight(accumulator, addressOrValue);
                    break;
                case TS:
                    programCounter = testAndSet(memory, addressOrValue, programCounter);
                    break;
                case CAS:
                    compareAndSwap(memory, addressOrValue);
                    break;
                default:
                    System.err.println("Unknown opcode: " + opcode);
                    break;
            }
        }
    }

    private static void compareAndSwap(List<Long> memory, long addressOrValue) {
        long oldValue = memory.get((int) addressOrValue);
        long compareValue = (addressOrValue >> 32) & 0xFFFFFFFFL;
        long newValue = addressOrValue & 0xFFFFFFFFL;
        if (oldValue == compareValue) {
            memory.set((int) addressOrValue, newValue);
        }
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

    private static void decrement(List<Long> memory, long addressOrValue) {
        if (addressOrValue < memory.size()) {
            memory.set((int) addressOrValue, memory.get((int) addressOrValue) - 1);
        }
    }

    private static void increment(List<Long> memory, long addressOrValue) {
        if (addressOrValue < memory.size()) {
            final var cast = (int) addressOrValue;
            memory.set(cast, memory.get(cast) + 1);
        }
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

    private static void out(long accumulator) {
        System.out.print(accumulator);
    }

    private static void store(List<Long> memory, long addressOrValue, long accumulator) {
        if (addressOrValue < memory.size()) {
            memory.set((int) addressOrValue, accumulator);
        } else {
            System.err.println("Address out of bounds.");
        }
    }

    private static long load(List<Long> memory, long addressOrValue) {
        long accumulator;
        accumulator = addressOrValue < memory.size() ? memory.get((int) addressOrValue) : 0;
        return accumulator;
    }

    private static void inputAndStore(List<Long> memory, Deque<Long> input, long addressOrValue) {
        if (input.isEmpty()) {
            throw new RuntimeException("Input queue is empty.");
        } else {
            final var polled = input.poll();
            set(memory, addressOrValue, polled);
        }
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

    private static void push(List<Long> memory, long addressOrValue) {
        set(memory, memory.get(STACK_POINTER_ADDRESS), addressOrValue);
        set(memory, STACK_POINTER_ADDRESS, memory.get(STACK_POINTER_ADDRESS) + 1);
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
}
