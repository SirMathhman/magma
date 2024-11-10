package magma;

import magma.api.Tuple;
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
import java.util.StringJoiner;

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
    public static final int SHL = 0x0C;
    public static final int SHR = 0x0D;
    public static final int TS = 0x0E;
    public static final int CAS = 0x0F;
    public static final int BYTES_PER_LONG = 8;
    public static final int STACK_POINTER_ADDRESS = 4;
    public static final int INITIAL_ADDRESS = 0;
    public static final int INCREMENT_ADDRESS = 2;
    public static final int REPEAT_ADDRESS = 3;
    public static final int STACK_POINTER_ADDRESS_OFFSET = 3;
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

    private static void execute(JavaList<Long> input) {
        System.out.println("Memory footprint: " + (input.size() * BYTES_PER_LONG) + " bytes");

        final var memory = compute(input);
        System.out.println();
        System.out.println("Final Memory State: " + formatHexList(memory));
    }

    private static JavaList<Long> compute(JavaList<Long> input) {
        final var memory = new JavaList<Long>();
        compute(memory, input);
        return memory;
    }

    private static void compute(JavaList<Long> initialMemory, JavaList<Long> initialInput) {
        var input = initialInput;
        var memory = initialMemory.add(computeInstruction(INPUT_AND_STORE, 1));

        long accumulator = 0;  // Holds current value for operations
        int programCounter = 0;

        while (programCounter < memory.size()) {
            final var option = memory.get(programCounter);
            if (option.isEmpty()) break;

            final long instructionUnsigned = option.orElse(0L);

            // Decode the instruction
            int opcode = (int) ((instructionUnsigned >> 56) & 0xFF);  // First 8 bits
            long addressOrValue = instructionUnsigned & 0x00FFFFFFFFFFFFFFL;  // Remaining 56 bits

            programCounter++;  // Move to next instruction by default

            // Execute based on opcode
            if (opcode == NO_OPERATION) continue;

            if (opcode == PUSH) {
                final var stackPointer = memory.get(STACK_POINTER_ADDRESS).orElse(0L);
                memory = set(memory, stackPointer, addressOrValue);
                memory = set(memory, STACK_POINTER_ADDRESS, stackPointer + 1);
            } else if (opcode == POP) {
                final var stackPointer = (long) memory.get(STACK_POINTER_ADDRESS).orElse(0L);
                final var max = Math.max(stackPointer - 1, 0);

                memory = set(memory, STACK_POINTER_ADDRESS, max);
                accumulator = memory.get((int) stackPointer).orElse(0L);
            } else if (opcode == INPUT_AND_LOAD) {
                final var polled = input.poll().orElse(new Tuple<>(0L, input));
                accumulator = polled.left();
                input = polled.right();
            } else if (opcode == INPUT_AND_STORE) {  // INP
                final var polled = input.poll().orElse(new Tuple<>(0L, input));
                final var left = polled.left();
                memory = set(memory, addressOrValue, left);
                input = polled.right();
            } else if (opcode == LOAD) {
                accumulator = memory.get((int) addressOrValue).orElse(0L);
            } else if (opcode == STORE) {
                memory = memory.set((int) addressOrValue, accumulator);
            } else if (opcode == OUT) {
                System.out.print(accumulator);
            } else if (opcode == ADD_ADDRESS) {
                accumulator += memory.get((int) addressOrValue).orElse(0L);
            } else if (opcode == ADD_VALUE) {
                accumulator += addressOrValue;
            } else if (opcode == SUB) {
                accumulator -= memory.get((int) addressOrValue).orElse(0L);
            } else if (opcode == INCREMENT) {
                final var cast = (int) addressOrValue;
                memory = memory.set(cast, memory.get(cast).orElse(0L) + 1);
            } else if (opcode == DEC) {
                final var value = memory.get((int) addressOrValue).orElse(0L);
                memory = memory.set((int) addressOrValue, value - 1);
            } else if (opcode == TAC) {
                if (accumulator < 0) programCounter = (int) addressOrValue;
            } else if (opcode == JUMP_ADDRESS) {  // JMP
                programCounter = (int) addressOrValue;
            } else if (opcode == HALT) {  // HRS
                return;
            } else if (opcode == SFT) {  // SFT
                int leftShift = (int) ((addressOrValue >> 8) & 0xFF);
                int rightShift = (int) (addressOrValue & 0xFF);
                accumulator = (accumulator << leftShift) >> rightShift;
            } else if (opcode == SHL) {  // SHL
                accumulator <<= addressOrValue;
            } else if (opcode == SHR) {  // SHR
                accumulator >>= addressOrValue;
            } else if (opcode == TS) {  // TS
                if (addressOrValue >= memory.size()) continue;

                if (memory.get((int) addressOrValue).orElse(0L) == 0) {
                    memory = memory.set((int) addressOrValue, 1L);
                } else {
                    programCounter = programCounter - 1;  // Retry this instruction if lock isn't available
                }
            } else if (opcode == CAS) {  // CAS
                var oldValue = memory.get((int) addressOrValue).orElse(0L);

                var compareValue = (addressOrValue >> 32) & 0xFFFFFFFFL;
                var newValue = addressOrValue & 0xFFFFFFFFL;
                if (oldValue == compareValue) {
                    memory = memory.set((int) addressOrValue, newValue);
                }
            } else {
                System.err.println("Unknown opcode: " + opcode);
            }
        }
    }

    private static JavaList<Long> set(JavaList<Long> memory, long address, long value) {
        return memory.set((int) address, value);
    }

    private static String formatHexList(JavaList<Long> list) {
        final var string = list.stream()
                .map(value -> Long.toString(value, 16))
                .foldLeft(new StringJoiner(", "), StringJoiner::add);

        return "[" + string + "]";
    }

    private static Result<JavaList<Long>, CompileError> assemble(String content) {
        return CASMLang.createRootRule()
                .parse(content)
                .mapValue(Assembler::parse);
    }

    private static JavaList<Long> parse(Node root) {
        return new JavaList<>();
    }

    static Result<String, IOException> readSafe(Path path) {
        try {
            return new Ok<>(Files.readString(path));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static long computeInstruction(int opCode, long addressOrValue) {
        if (opCode < 0x00 || opCode > 0xFF) {
            throw new IllegalArgumentException("Opcode must be an 8-bit value (0x00 to 0xFF).");
        }
        if (addressOrValue < 0 || addressOrValue > 0x00FFFFFFFFFFFFFFL) {
            throw new IllegalArgumentException("Address/Value must be a 56-bit value (0x00 to 0x00FFFFFFFFFFFFFF).");
        }

        return ((long) opCode << 56) | addressOrValue;
    }
}
