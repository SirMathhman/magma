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
    public static final int TEST_AND_SET = 0x0E;
    public static final int COMPARE_AND_SWAP = 0x0F;
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
        var current = state;
        while (current.programCounter < current.memory.size()) {
            final var option = perform(current);
            if (option.isEmpty()) break;

            final var result = option.orElse(new Ok<>(current));
            if (result.isErr()) {
                return new Err<>(result.findErr().orElse(null));
            } else if (result.isOk()) {
                current = result.findValue().orElse(current);
            }
        }

        return new Ok<>(current.memory);
    }

    private static Option<Result<State, RuntimeError>> perform(State current) {
        final var option = current.memory.get(current.programCounter);
        if (option.isEmpty()) new None<>();

        final long instructionUnsigned = option.orElse(0L);

        // Decode the instruction
        int opcode = (int) ((instructionUnsigned >> 56) & 0xFF);  // First 8 bits
        long addressOrValue = instructionUnsigned & 0x00FFFFFFFFFFFFFFL;  // Remaining 56 bits

        final var withProgramCounter = current.withProgramCounter(current.programCounter + 1);// Move to next instruction by default

        if (opcode == NO_OPERATION) return new Some<>(new Ok<>(current));
        if (opcode == PUSH) {
            final var stackPointer = withProgramCounter.memory.get(STACK_POINTER_ADDRESS).orElse(0L);
            return new Some<>(new Ok<>(withProgramCounter
                    .set((int) stackPointer.longValue(), addressOrValue)
                    .set(STACK_POINTER_ADDRESS, stackPointer + 1)));

        }
        if (opcode == POP) {
            final var stackPointer = (long) withProgramCounter.memory.get(STACK_POINTER_ADDRESS).orElse(0L);
            final var max = Math.max(stackPointer - 1, 0);

            return new Some<>(new Ok<>(withProgramCounter
                    .set(STACK_POINTER_ADDRESS, max)
                    .withAccumulator(withProgramCounter.memory.get((int) stackPointer).orElse(0L))));
        }
        if (opcode == INPUT_AND_LOAD) {
            final var polled = withProgramCounter.input.poll().orElse(new Tuple<>(0L, withProgramCounter.input));

            return new Some<>(new Ok<>(withProgramCounter
                    .withAccumulator(polled.left())
                    .withInput(polled.right())));
        }
        if (opcode == INPUT_AND_STORE) {  // INP
            final var polled = withProgramCounter.input.poll().orElse(new Tuple<>(0L, withProgramCounter.input));
            final var left = polled.left();

            return new Some<>(new Ok<>(withProgramCounter
                    .withMemory(withProgramCounter.memory.set((int) addressOrValue, left))
                    .withInput(polled.right())));
        }
        if (opcode == LOAD) {
            return new Some<>(new Ok<>(withProgramCounter.withAccumulator(withProgramCounter.memory.get((int) addressOrValue).orElse(0L))));
        }
        if (opcode == STORE) {
            return new Some<>(new Ok<>(withProgramCounter.set((int) addressOrValue, withProgramCounter.accumulator)));
        }
        if (opcode == OUT) {
            System.out.print(withProgramCounter.accumulator);
        }
        if (opcode == ADD_ADDRESS) {
            return new Some<>(new Ok<>(withProgramCounter.withAccumulator(withProgramCounter.accumulator + withProgramCounter.memory.get((int) addressOrValue).orElse(0L))));
        }
        if (opcode == ADD_VALUE) {
            return new Some<>(new Ok<>(withProgramCounter.withAccumulator(withProgramCounter.accumulator + addressOrValue)));
        }
        if (opcode == SUB) {
            return new Some<>(new Ok<>(withProgramCounter.withAccumulator(withProgramCounter.accumulator - withProgramCounter.memory.get((int) addressOrValue).orElse(0L))));
        }
        if (opcode == INCREMENT) {
            final var cast = (int) addressOrValue;
            return new Some<>(new Ok<>(withProgramCounter.set(cast, withProgramCounter.memory.get(cast).orElse(0L) + 1)));
        }
        if (opcode == DEC) {
            final var value = withProgramCounter.memory.get((int) addressOrValue).orElse(0L);
            return new Some<>(new Ok<>(withProgramCounter.set((int) addressOrValue, value - 1)));
        }
        if (opcode == TAC) {
            if (withProgramCounter.accumulator < 0) withProgramCounter.withProgramCounter((int) addressOrValue);
        }
        if (opcode == JUMP_ADDRESS) {
            return new Some<>(new Ok<>(withProgramCounter.withProgramCounter((int) addressOrValue)));
        }
        if (opcode == HALT) {
            return new None<>();
        }
        if (opcode == SFT) {  // SFT
            int leftShift = (int) ((addressOrValue >> 8) & 0xFF);
            int rightShift = (int) (addressOrValue & 0xFF);
            return new Some<>(new Ok<>(withProgramCounter.withAccumulator((withProgramCounter.accumulator << leftShift) >> rightShift)));
        }
        if (opcode == SHL) {  // SHL
            return new Some<>(new Ok<>(withProgramCounter.withAccumulator(withProgramCounter.accumulator << addressOrValue)));
        }
        if (opcode == SHR) {  // SHR
            return new Some<>(new Ok<>(withProgramCounter.withAccumulator(withProgramCounter.accumulator >> addressOrValue)));
        }

        final var withProgramCounter1 = testAndSet(withProgramCounter, opcode, (int) addressOrValue);
        if (withProgramCounter1 != null) return withProgramCounter1;

        return compareAndSwap(withProgramCounter, opcode, addressOrValue)
                .or(() -> new Some<>(new Err<>(new RuntimeError("Unknown opcode: " + opcode))));
    }

    private static Option<Result<State, RuntimeError>> testAndSet(State state, int opcode, int addressOrValue) {
        if (opcode != TEST_AND_SET) return new None<>();

        return new Some<>(state.get(addressOrValue).mapValue(value -> value == 0
                ? state.set(addressOrValue, 1L)
                : state.withProgramCounter(state.programCounter - 1)));
    }

    private static Option<Result<State, RuntimeError>> compareAndSwap(State state, int opcode, long combinedValue) {
        if (opcode != COMPARE_AND_SWAP) return new None<>();

        final var casted = (int) combinedValue;
        return state.get(casted).match(oldValue -> {
            var compareValue = (combinedValue >> 32) & 0xFFFFFFFFL;
            if (oldValue != compareValue) return new None<>();

            var newValue = combinedValue & 0xFFFFFFFFL;
            final var set = state.set(casted, newValue);
            return new Some<>(new Ok<>(set));
        }, err -> new Some<>(new Err<>(err)));
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

    public static Result<String, IOException> readSafe(Path path) {
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
        private JavaList<Long> input;
        private JavaList<Long> memory;
        private int programCounter;
        private long accumulator;

        private State(JavaList<Long> input, JavaList<Long> memory, int programCounter, long accumulator) {
            this.withInput(input);
            this.withMemory(memory);
            this.withProgramCounter(programCounter);
            this.withAccumulator(accumulator);
        }

        private State set(int addressOrValue, long newValue) {
            return withMemory(memory.set(addressOrValue, newValue));
        }

        public State withInput(JavaList<Long> input) {
            return new State(input, memory, programCounter, accumulator);
        }

        public State withMemory(JavaList<Long> memory) {
            return new State(input, memory, programCounter, accumulator);
        }

        public State withProgramCounter(int programCounter) {
            return new State(input, memory, programCounter, accumulator);
        }

        public State withAccumulator(long accumulator) {
            return new State(input, memory, programCounter, accumulator);
        }

        public Result<Long, RuntimeError> get(int address) {
            return memory.get(address)
                    .<Result<Long, RuntimeError>>map(Ok::new)
                    .orElseGet(() -> new Err<>(new RuntimeError("Value at '" + address + "' not defined.")));
        }
    }
}
