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
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.CASMLang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
    public static final String OP_CODE = "op-code";
    public static final String ADDRESS_OR_VALUE = "address-or-value";
    public static final String LABEL = "address";
    public static final String DATA_VALUE = "data";
    public static final String DATA_TYPE = "data";
    public static final String INSTRUCTION_TYPE = "instruction";
    public static final long PROGRAM_COUNTER_ADDRESS = 4L;
    public static final String PROGRAM_COUNTER = "program-counter";
    public static final String INIT = "__init__";
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
        memory.add(createInstruction(new MapNode()
                .withString(OP_CODE, Integer.toUnsignedString(INPUT_AND_STORE, 16))
                .withString(ADDRESS_OR_VALUE, Long.toUnsignedString(1L, 16))));

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
                    set(memory, memory.get(STACK_POINTER_ADDRESS), addressOrValue);
                    set(memory, STACK_POINTER_ADDRESS, memory.get(STACK_POINTER_ADDRESS) + 1);
                    break;
                case POP:
                    set(memory, STACK_POINTER_ADDRESS, Math.max(memory.get(STACK_POINTER_ADDRESS) - 1, 0));
                    accumulator = memory.get((int) memory.get(STACK_POINTER_ADDRESS).longValue());
                    break;
                case INPUT_AND_LOAD:  // INP
                    if (input.isEmpty()) {
                        throw new RuntimeException("Input queue is empty.");
                    } else {
                        accumulator = input.poll();
                    }
                    break;
                case INPUT_AND_STORE:  // INP
                    if (input.isEmpty()) {
                        throw new RuntimeException("Input queue is empty.");
                    } else {
                        final var polled = input.poll();
                        set(memory, addressOrValue, polled);
                        break;
                    }
                case LOAD:  // LOAD
                    accumulator = addressOrValue < memory.size() ? memory.get((int) addressOrValue) : 0;
                    break;
                case STORE:  // STO
                    if (addressOrValue < memory.size()) {
                        memory.set((int) addressOrValue, accumulator);
                    } else {
                        System.err.println("Address out of bounds.");
                    }
                    break;
                case OUT:  // OUT
                    System.out.print(accumulator);
                    break;
                case ADD_ADDRESS:  // ADD
                    if (addressOrValue < memory.size()) {
                        accumulator += memory.get((int) addressOrValue);
                    } else {
                        System.err.println("Address out of bounds.");
                    }
                    break;
                case ADD_VALUE:
                    accumulator += addressOrValue;
                    break;
                case SUB:  // SUB
                    if (addressOrValue < memory.size()) {
                        accumulator -= memory.get((int) addressOrValue);
                    } else {
                        System.err.println("Address out of bounds.");
                    }
                    break;
                case INCREMENT:  // INC
                    if (addressOrValue < memory.size()) {
                        final var cast = (int) addressOrValue;
                        memory.set(cast, memory.get(cast) + 1);
                    }
                    break;
                case DEC:  // DEC
                    if (addressOrValue < memory.size()) {
                        memory.set((int) addressOrValue, memory.get((int) addressOrValue) - 1);
                    }
                    break;
                case TAC:  // TAC
                    if (accumulator < 0) {
                        programCounter = (int) addressOrValue;
                    }
                    break;
                case JUMP_ADDRESS:  // JMP
                    programCounter = (int) addressOrValue;
                    break;
                case HALT:  // HRS
                    return;
                case SFT:  // SFT
                    int leftShift = (int) ((addressOrValue >> 8) & 0xFF);
                    int rightShift = (int) (addressOrValue & 0xFF);
                    accumulator = (accumulator << leftShift) >> rightShift;
                    break;
                case SHL:  // SHL
                    accumulator <<= addressOrValue;
                    break;
                case SHR:  // SHR
                    accumulator >>= addressOrValue;
                    break;
                case TS:  // TS
                    if (addressOrValue < memory.size()) {
                        if (memory.get((int) addressOrValue) == 0) {
                            memory.set((int) addressOrValue, 1L);
                        } else {
                            programCounter--;  // Retry this instruction if lock isn't available
                        }
                    }
                    break;
                case CAS:  // CAS
                    long oldValue = memory.get((int) addressOrValue);
                    long compareValue = (addressOrValue >> 32) & 0xFFFFFFFFL;
                    long newValue = addressOrValue & 0xFFFFFFFFL;
                    if (oldValue == compareValue) {
                        memory.set((int) addressOrValue, newValue);
                    }
                    break;
                default:
                    System.err.println("Unknown opcode: " + opcode);
                    break;
            }
        }
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
        var labels = new HashMap<String, Long>();
        labels.put(INIT, 0L);

        final var list = new ArrayList<Node>();
        set(list, 2, new MapNode(INSTRUCTION_TYPE)
                .withString(OP_CODE, Integer.toUnsignedString(JUMP_ADDRESS, 16))
                .withString(LABEL, INIT));

        labels.put(PROGRAM_COUNTER, PROGRAM_COUNTER_ADDRESS);
        set(list, (int) PROGRAM_COUNTER_ADDRESS, 0);

        set(list, 3, new MapNode(INSTRUCTION_TYPE)
                .withString(OP_CODE, Integer.toUnsignedString(JUMP_ADDRESS, 16))
                .withString(LABEL, INIT));
        set(list, 2, new MapNode(INSTRUCTION_TYPE)
                .withString(OP_CODE, Integer.toUnsignedString(INCREMENT, 16))
                .withString(LABEL, "program-counter"));

        final var LOOP_OFFSET = 5;

        final var data = Map.of(
                "first", 100L,
                "second", 200L,
                "third", 300L
        );

        final var entryList = data.keySet()
                .stream()
                .sorted()
                .toList();

        for (int i = 0; i < entryList.size(); i++) {
            final var label = entryList.get(i);
            final var value = data.get(label);

            final var address = LOOP_OFFSET + i;
            labels.put(label, (long) address);
            set(list, address, value);
        }

        final var initialAddress = LOOP_OFFSET + data.size();

        labels.put("start", (long) initialAddress);
        final var start = List.of(
                new MapNode(INSTRUCTION_TYPE)
                        .withString(OP_CODE, Integer.toUnsignedString(LOAD, 16))
                        .withString(LABEL, PROGRAM_COUNTER),
                new MapNode(INSTRUCTION_TYPE)
                        .withString(OP_CODE, Integer.toUnsignedString(ADD_VALUE, 16))
                        .withString(ADDRESS_OR_VALUE, Long.toUnsignedString(3, 16)),
                new MapNode(INSTRUCTION_TYPE)
                        .withString(OP_CODE, Integer.toUnsignedString(STORE, 16))
                        .withString(LABEL, PROGRAM_COUNTER),
                new MapNode(INSTRUCTION_TYPE)
                        .withString(OP_CODE, Integer.toUnsignedString(JUMP_ADDRESS, 16))
                        .withString(LABEL, "exit")
        );

        for (int i = 0; i < start.size(); i++) {
            Node node = start.get(i);
            set(list, initialAddress + i, node);
        }

        var exit = List.of(
                new MapNode(INSTRUCTION_TYPE)
                        .withString(OP_CODE, Integer.toUnsignedString(LOAD, 16))
                        .withString(LABEL, PROGRAM_COUNTER),
                new MapNode(INSTRUCTION_TYPE)
                        .withString(OP_CODE, Integer.toUnsignedString(OUT, 16)),
                new MapNode(INSTRUCTION_TYPE)
                        .withString(OP_CODE, Integer.toUnsignedString(HALT, 16))
                        .withString(ADDRESS_OR_VALUE, Long.toUnsignedString(0, 16))
        );

        var haltStart = initialAddress + 4;
        labels.put("exit", (long) haltStart);
        for (int i = 0; i < exit.size(); i++) {
            set(list, haltStart + i, exit.get(i));
        }

        set(list, 3, new MapNode(INSTRUCTION_TYPE)
                .withString(OP_CODE, Integer.toUnsignedString(JUMP_ADDRESS, 16))
                .withString(LABEL, "start"));

        return list.stream()
                .map(node -> resolveLabel(labels, node))
                .map(Assembler::computeBinary)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private static long computeBinary(Node node) {
        if (node.is(INSTRUCTION_TYPE)) return createInstruction(node);
        if (node.is(DATA_TYPE)) return Long.parseUnsignedLong(node.findString(DATA_VALUE).orElse(""), 16);
        throw new UnsupportedOperationException("Unknown node: " + node);
    }

    private static Node resolveLabel(Map<String, Long> labels, Node node) {
        if (!node.is(INSTRUCTION_TYPE)) return node;

        final var option = node.findString(LABEL);
        if (option.isEmpty()) return node;

        final var label = option.orElse("");
        final var addressOrValue = labels.get(label);
        return node.withString(ADDRESS_OR_VALUE, Long.toUnsignedString(addressOrValue, 16));
    }

    private static void set(List<Node> list, int instructionAddress, long data) {
        set(list, instructionAddress, new MapNode(DATA_TYPE)
                .withString(DATA_VALUE, Long.toUnsignedString(data, 16)));
    }

    private static void set(List<Node> list, int instructionAddress, Node instruction) {
        list.add(new MapNode(INSTRUCTION_TYPE)
                .withString(OP_CODE, Integer.toUnsignedString(INPUT_AND_STORE, 16))
                .withString(ADDRESS_OR_VALUE, Long.toUnsignedString(instructionAddress, 16)));

        list.add(instruction);
    }

    static Result<String, IOException> readSafe(Path path) {
        try {
            return new Ok<>(Files.readString(path));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static long createInstruction(Node node) {
        final var opCode = Integer.parseUnsignedInt(node.findString("op-code").orElse(""), 16);
        final var addressOrValue = node.findString("address-or-value")
                .map(value -> Long.parseUnsignedLong(value, 16))
                .orElse(0L);

        if (opCode < 0x00 || opCode > 0xFF) {
            throw new IllegalArgumentException("Opcode must be an 8-bit value (0x00 to 0xFF).");
        }
        if (addressOrValue < 0 || addressOrValue > 0x00FFFFFFFFFFFFFFL) {
            throw new IllegalArgumentException("Address/Value must be a 56-bit value (0x00 to 0x00FFFFFFFFFFFFFF).");
        }

        return ((long) opCode << 56) | addressOrValue;
    }
}
