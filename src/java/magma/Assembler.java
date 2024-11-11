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
import magma.app.compile.ResultStream;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.lang.CASMLang;
import magma.java.JavaStreams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static magma.app.compile.lang.CASMLang.*;

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
    public static final long PROGRAM_COUNTER_ADDRESS = 4L;
    public static final String PROGRAM_COUNTER = "%pc";
    public static final String INIT = "__init__";
    public static final int LOOP_OFFSET = 5;
    public static final String MAIN = "__main__";
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
        Node node = new MapNode()
                .withString(OP_CODE, Integer.toUnsignedString(INPUT_AND_STORE, 16))
                .withString(CASMLang.ADDRESS_OR_VALUE, Long.toUnsignedString(1L, 16));
        memory.add(createInstruction(node).match(value -> value, err -> {
            throw new IllegalStateException(err.message());
        }));

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
                .flatMapValue(Assembler::parse);
    }

    private static Result<Deque<Long>, CompileError> parse(Node root) {
        final var data = new HashMap<String, Long>();
        final var program = new ArrayList<Tuple<String, List<Node>>>();

        final var children = root.findNodeList(CHILDREN).orElse(Collections.emptyList());
        for (Node section : children) {
            if (!section.is(SECTION_TYPE)) continue;

            final var name = section.findString(GROUP_NAME).orElse("");

            final var sectionChildren = section.findNodeList(CHILDREN).orElse(Collections.emptyList());
            if (name.equals("data")) {
                for (var dataNode : sectionChildren) {
                    if (!dataNode.is(DATA_TYPE)) continue;

                    final var dataName = dataNode.findString(DATA_NAME).orElse("");
                    final var value = dataNode.findNode(DATA_VALUE).orElse(new MapNode());
                    long actualValue = 0L;
                    if (value.is(NUMBER_TYPE)) {
                        final var numberValue = value.findString(NUMBER_VALUE).orElse("");
                        actualValue = Long.parseLong(numberValue, 10);
                    }

                    data.put(dataName, actualValue);
                }
            } else if (name.equals("program")) {
                for (Node label : sectionChildren) {
                    if (!label.is(LABEL_TYPE)) continue;

                    final var labelName = label.findString(GROUP_NAME).orElse("");
                    final var labelChildren = label.findNodeList(CHILDREN)
                            .orElse(Collections.emptyList())
                            .stream()
                            .filter(value -> value.is(INSTRUCTION_TYPE))
                            .map(instruction -> {
                                final var code = instruction.findString(OP_CODE).orElse("");
                                final var codeNumeric = switch (code) {
                                    case "jump" -> JUMP_ADDRESS;
                                    case "load" -> LOAD;
                                    case "out" -> OUT;
                                    case "halt" -> HALT;
                                    default -> throw new IllegalArgumentException("Unknown code: " + code);
                                };

                                return instruction.withString(OP_CODE, Long.toString(codeNumeric, 16));
                            })
                            .toList();

                    program.add(new Tuple<>(labelName, labelChildren));
                }
            }
        }

        int entryIndex = -1;
        final var programCopy = new ArrayList<>(program);
        for (int i = 0; i < programCopy.size(); i++) {
            Tuple<String, List<Node>> tuple = programCopy.get(i);
            final var label = tuple.left();
            if (label.equals(MAIN)) {
                entryIndex = i;
                break;
            }
        }

        final var newFirst = programCopy.get(entryIndex).<List<Node>>mapRight(right -> {
            final var copy = new ArrayList<>(List.of(new MapNode(INSTRUCTION_TYPE)
                            .withString(OP_CODE, Integer.toUnsignedString(LOAD, 16))
                            .withString(INSTRUCTION_LABEL, PROGRAM_COUNTER),
                    new MapNode(INSTRUCTION_TYPE)
                            .withString(OP_CODE, Integer.toUnsignedString(ADD_VALUE, 16))
                            .withString(ADDRESS_OR_VALUE, Long.toUnsignedString(3, 16)),
                    new MapNode(INSTRUCTION_TYPE)
                            .withString(OP_CODE, Integer.toUnsignedString(STORE, 16))
                            .withString(INSTRUCTION_LABEL, PROGRAM_COUNTER)));
            copy.addAll(right);
            return copy;
        });

        programCopy.set(entryIndex, newFirst);

        var labels = new HashMap<String, Long>();
        labels.put(INIT, 0L);

        final var list = new ArrayList<Node>();
        set(list, 2, new MapNode(CASMLang.INSTRUCTION_TYPE)
                .withString(OP_CODE, Integer.toUnsignedString(JUMP_ADDRESS, 16))
                .withString(INSTRUCTION_LABEL, INIT));

        labels.put(PROGRAM_COUNTER, PROGRAM_COUNTER_ADDRESS);
        set(list, (int) PROGRAM_COUNTER_ADDRESS, 0);

        set(list, 3, new MapNode(CASMLang.INSTRUCTION_TYPE)
                .withString(OP_CODE, Integer.toUnsignedString(JUMP_ADDRESS, 16))
                .withString(INSTRUCTION_LABEL, INIT));
        set(list, 2, new MapNode(CASMLang.INSTRUCTION_TYPE)
                .withString(OP_CODE, Integer.toUnsignedString(INCREMENT, 16))
                .withString(INSTRUCTION_LABEL, PROGRAM_COUNTER));

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

        var pointer = LOOP_OFFSET + data.size();
        for (Tuple<String, List<Node>> tuple : programCopy) {
            labels.put(tuple.left(), (long) pointer);
            for (Node item : tuple.right()) {
                set(list, pointer, item);
                pointer++;
            }
        }

        set(list, 3, new MapNode(CASMLang.INSTRUCTION_TYPE)
                .withString(OP_CODE, Integer.toUnsignedString(JUMP_ADDRESS, 16))
                .withString(INSTRUCTION_LABEL, MAIN));

        return JavaStreams.fromList(list)
                .map(node -> resolveLabel(labels, node))
                .map(node1 -> computeBinary(node1))
                .into(ResultStream::new)
                .foldResultsLeft(new LinkedList<Long>(), (longs, aLong) -> {
                    longs.add(aLong);
                    return longs;
                });
    }

    private static Result<Long, CompileError> computeBinary(Node node) {
        if (node.is(CASMLang.INSTRUCTION_TYPE)) return createInstruction(node);

        if (node.is(CASMLang.DATA_TYPE)) {
            final var dataValue = node.findString(DATA_VALUE);
            if (dataValue.isEmpty())
                return new Err<>(new CompileError("No data value present", new NodeContext(node)));

            return new Ok<>(Long.parseUnsignedLong(dataValue.orElse(""), 16));
        }

        return new Err<>(new CompileError("Unknown node", new NodeContext(node)));
    }

    private static Node resolveLabel(Map<String, Long> labels, Node node) {
        if (!node.is(CASMLang.INSTRUCTION_TYPE)) return node;

        final var option = node.findString(INSTRUCTION_LABEL);
        if (option.isEmpty()) return node;

        final var label = option.orElse("");
        final var addressOrValue = labels.get(label);
        return node.withString(CASMLang.ADDRESS_OR_VALUE, Long.toUnsignedString(addressOrValue, 16));
    }

    private static void set(List<Node> list, int instructionAddress, long data) {
        set(list, instructionAddress, new MapNode(CASMLang.DATA_TYPE)
                .withString(CASMLang.DATA_VALUE, Long.toUnsignedString(data, 16)));
    }

    private static void set(List<Node> list, int instructionAddress, Node instruction) {
        list.add(new MapNode(CASMLang.INSTRUCTION_TYPE)
                .withString(OP_CODE, Integer.toUnsignedString(INPUT_AND_STORE, 16))
                .withString(CASMLang.ADDRESS_OR_VALUE, Long.toUnsignedString(instructionAddress, 16)));

        list.add(instruction);
    }

    static Result<String, IOException> readSafe(Path path) {
        try {
            return new Ok<>(Files.readString(path));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Result<Long, CompileError> createInstruction(Node node) {
        final var opCodeOption = node.findString(OP_CODE);
        if (opCodeOption.isEmpty()) return new Err<>(new CompileError("No op code present", new NodeContext(node)));

        final var opCode = Integer.parseUnsignedInt(opCodeOption.orElse(""), 16);

        final var option = node.findString(ADDRESS_OR_VALUE);
        if (option.isEmpty())
            return new Err<>(new CompileError("No address or value present", new NodeContext(node)));

        final var addressOrValue = option
                .map(value -> Long.parseUnsignedLong(value, 16))
                .orElse(0L);

        if (opCode < 0x00 || opCode > 0xFF) {
            throw new IllegalArgumentException("Opcode must be an 8-bit value (0x00 to 0xFF).");
        }
        if (addressOrValue < 0 || addressOrValue > 0x00FFFFFFFFFFFFFFL) {
            throw new IllegalArgumentException("Address/Value must be a 56-bit value (0x00 to 0x00FFFFFFFFFFFFFF).");
        }

        return new Ok<>(((long) opCode << 56) | addressOrValue);
    }
}
