package magma.assemble;

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

        interpret(input).consume(memory -> {
            System.out.println();
            System.out.println("Final Memory State: " + formatHexList(memory));
        }, err -> System.err.println(err.message));
    }

    private static Result<JavaList<Long>, RuntimeError> interpret(JavaList<Long> input) {
        return computeInstruction(INPUT_AND_STORE, 1)
                .mapValue(new JavaList<Long>()::add)
                .flatMapValue(result -> interpretWithInitial(result, input));
    }

    private static Result<JavaList<Long>, RuntimeError> interpretWithInitial(JavaList<Long> initialMemory, JavaList<Long> input) {
        long accumulator = 0;
        int programCounter = 0;
        return interpretWithState(new State(input, initialMemory, programCounter, accumulator));
    }

    private static Result<JavaList<Long>, RuntimeError> interpretWithState(State state) {
        while (state.programCounter < state.memory.size()) {
            final var option = state.memory.get(state.programCounter);
            if (option.isEmpty()) break;

            final long instructionUnsigned = option.orElse(0L);

            // Decode the instruction
            int opcode = (int) ((instructionUnsigned >> 56) & 0xFF);  // First 8 bits
            long addressOrValue = instructionUnsigned & 0x00FFFFFFFFFFFFFFL;  // Remaining 56 bits

            state.programCounter++;  // Move to next instruction by default

            if (opcode == NO_OPERATION) continue;
            if (opcode == PUSH) {
                final var stackPointer = state.memory.get(STACK_POINTER_ADDRESS).orElse(0L);
                state.memory = state.memory.set((int) stackPointer.longValue(), addressOrValue);
                state.memory = state.memory.set(STACK_POINTER_ADDRESS, stackPointer + 1);
            } else if (opcode == POP) {
                final var stackPointer = (long) state.memory.get(STACK_POINTER_ADDRESS).orElse(0L);
                final var max = Math.max(stackPointer - 1, 0);

                state.memory = state.memory.set(STACK_POINTER_ADDRESS, max);
                state.accumulator = state.memory.get((int) stackPointer).orElse(0L);
            } else if (opcode == INPUT_AND_LOAD) {
                final var polled = state.input.poll().orElse(new Tuple<>(0L, state.input));
                state.accumulator = polled.left();
                state.input = polled.right();
            } else if (opcode == INPUT_AND_STORE) {  // INP
                final var polled = state.input.poll().orElse(new Tuple<>(0L, state.input));
                final var left = polled.left();
                state.memory = state.memory.set((int) addressOrValue, left);
                state.input = polled.right();
            } else if (opcode == LOAD) {
                state.accumulator = state.memory.get((int) addressOrValue).orElse(0L);
            } else if (opcode == STORE) {
                state.memory = state.memory.set((int) addressOrValue, state.accumulator);
            } else if (opcode == OUT) {
                System.out.print(state.accumulator);
            } else if (opcode == ADD_ADDRESS) {
                state.accumulator += state.memory.get((int) addressOrValue).orElse(0L);
            } else if (opcode == ADD_VALUE) {
                state.accumulator += addressOrValue;
            } else if (opcode == SUB) {
                state.accumulator -= state.memory.get((int) addressOrValue).orElse(0L);
            } else if (opcode == INCREMENT) {
                final var cast = (int) addressOrValue;
                state.memory = state.memory.set(cast, state.memory.get(cast).orElse(0L) + 1);
            } else if (opcode == DEC) {
                final var value = state.memory.get((int) addressOrValue).orElse(0L);
                state.memory = state.memory.set((int) addressOrValue, value - 1);
            } else if (opcode == TAC) {
                if (state.accumulator < 0) state.programCounter = (int) addressOrValue;
            } else if (opcode == JUMP_ADDRESS) {  // JMP
                state.programCounter = (int) addressOrValue;
            } else if (opcode == HALT) {  // HRS
                return new Ok<>(state.memory);
            } else if (opcode == SFT) {  // SFT
                int leftShift = (int) ((addressOrValue >> 8) & 0xFF);
                int rightShift = (int) (addressOrValue & 0xFF);
                state.accumulator = (state.accumulator << leftShift) >> rightShift;
            } else if (opcode == SHL) {  // SHL
                state.accumulator <<= addressOrValue;
            } else if (opcode == SHR) {  // SHR
                state.accumulator >>= addressOrValue;
            } else if (opcode == TS) {  // TS
                if (addressOrValue >= state.memory.size()) continue;

                if (state.memory.get((int) addressOrValue).orElse(0L) == 0) {
                    state.memory = state.memory.set((int) addressOrValue, 1L);
                } else {
                    state.programCounter = state.programCounter - 1;  // Retry this instruction if lock isn't available
                }
            } else if (opcode == CAS) {  // CAS
                var oldValue = state.memory.get((int) addressOrValue).orElse(0L);

                var compareValue = (addressOrValue >> 32) & 0xFFFFFFFFL;
                var newValue = addressOrValue & 0xFFFFFFFFL;
                if (oldValue == compareValue) {
                    state.memory = state.memory.set((int) addressOrValue, newValue);
                }
            } else {
                return new Err<>(new RuntimeError("Unknown opcode: " + opcode));
            }
        }

        return new Ok<>(state.memory);
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

    private static Result<Long, RuntimeError> computeInstruction(int opCode, long addressOrValue) {
        if (opCode < 0x00 || opCode > 0xFF) {
            return new Err<>(new RuntimeError("Opcode must be an 8-bit value (0x00 to 0xFF)."));
        }
        if (addressOrValue < 0 || addressOrValue > 0x00FFFFFFFFFFFFFFL) {
            return new Err<>(new RuntimeError("Address/Value must be a 56-bit value (0x00 to 0x00FFFFFFFFFFFFFF)."));
        }

        return new Ok<>(((long) opCode << 56) | addressOrValue);
    }

    public static final class State {
        public JavaList<Long> input;
        public JavaList<Long> memory;
        public int programCounter;
        public long accumulator;

        private State(JavaList<Long> input, JavaList<Long> memory, int programCounter, long accumulator) {
            this.input = input;
            this.memory = memory;
            this.programCounter = programCounter;
            this.accumulator = accumulator;
        }
    }
}
