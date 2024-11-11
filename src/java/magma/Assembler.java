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
import magma.app.compile.error.StringContext;
import magma.java.JavaList;
import magma.java.JavaStreams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static magma.app.compile.lang.CASMLang.*;

public class Assembler {
    public static final Path ROOT = Paths.get(".", "src", "magma");
    public static final Path TARGET = ROOT.resolve("main.casm");

    public static final int INPUT_AND_LOAD = 0x00;
    public static final int INPUT_AND_STORE = 0x10;
    public static final int LOAD_DIRECT = 0x01;
    public static final int LOAD_VALUE = 0x15;
    public static final int LOAD_ACCUMULATOR = 0x17;
    public static final int STORE_INDIRECT = 0x02;
    public static final int OUT = 0x03;
    public static final int ADD = 4;
    public static final int SUBTRACT = 0x05;
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
    public static final long STACK_POINTER_ADDRESS = 4L;
    public static final String STACK_POINTER_COUNTER = "%sp";
    public static final String INIT = "__init__";
    public static final int LOOP_OFFSET = 5;
    public static final String MAIN = "__main__";
    private static final int LOAD_INDIRECT = 0x19;
    private static final int PUSH = 0x11;
    private static final int POP = 0x12;
    private static final int NO_OPERATION = 0x13;
    private static final int STORE_DIRECT = 0x16;
    private static final int JUMP_ACCUMULATOR = 0x18;

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
        return assemble(input).mapErr(ApplicationError::new).match(tuple -> {
            execute(tuple);
            return new None<>();
        }, Some::new);
    }

    private static void execute(Tuple<LinkedList<Long>, Map<String, Long>> tuple) {
        var input = tuple.left();
        final var labels = tuple.right();

        System.out.println("Memory footprint: " + (input.size() * BYTES_PER_LONG) + " bytes");
        compute(input, labels);
    }

    private static void compute(LinkedList<Long> input, Map<String, Long> labels) {
        final var memory = new ArrayList<Long>();
        boolean finished = false;
        var node = new MapNode()
                .withInt(OP_CODE, INPUT_AND_STORE)
                .withInt(ADDRESS_OR_VALUE, 1);

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
                    final var pushed = memory.get((int) STACK_POINTER_ADDRESS);
                    final var next = pushed + 1;

                    while(!(next < memory.size())) memory.add(0L);
                    memory.set((int) next, accumulator);
                    memory.set((int) STACK_POINTER_ADDRESS, next);
                    break;
                case POP:
                    final var popped = memory.get((int) STACK_POINTER_ADDRESS);
                    accumulator = memory.get(Math.toIntExact(popped));
                    memory.set((int) STACK_POINTER_ADDRESS, popped - 1);
                    break;
                case INPUT_AND_LOAD:  // INP
                    if (((Deque<Long>) input).isEmpty()) {
                        throw new RuntimeException("Input queue is empty.");
                    } else {
                        accumulator = ((Deque<Long>) input).poll();
                    }
                    break;
                case INPUT_AND_STORE:  // INP
                    if (((Deque<Long>) input).isEmpty()) {
                        throw new RuntimeException("Input queue is empty.");
                    } else {
                        final var polled = ((Deque<Long>) input).poll();
                        set(memory, addressOrValue, polled);
                        break;
                    }
                case LOAD_DIRECT:  // LOAD
                    accumulator = addressOrValue < memory.size() ? memory.get((int) addressOrValue) : 0;
                    break;
                case LOAD_INDIRECT:
                    final var first = memory.get((int) addressOrValue);
                    accumulator = memory.get(Math.toIntExact(first));
                    break;
                case LOAD_VALUE:
                    accumulator = addressOrValue;
                    break;
                case LOAD_ACCUMULATOR:
                    accumulator = memory.get((int) accumulator);
                    break;
                case STORE_DIRECT:  // STO
                    if (addressOrValue < memory.size()) {
                        memory.set((int) addressOrValue, accumulator);
                    } else {
                        System.err.println("Address out of bounds.");
                    }
                    break;
                case STORE_INDIRECT:
                    if (addressOrValue < memory.size()) {
                        final var index = memory.get((int) addressOrValue);

                        while (!(index < memory.size())) {
                            memory.add(0L);
                        }

                        memory.set((int) index.longValue(), accumulator);
                    } else {
                        System.err.println("Address out of bounds.");
                    }
                    break;
                case OUT:  // OUT
                    System.out.print(accumulator);
                    break;
                case ADD:  // ADD
                    if (addressOrValue < memory.size()) {
                        accumulator += memory.get((int) addressOrValue);
                    } else {
                        System.err.println("Address out of bounds.");
                    }
                    break;
                case SUBTRACT:  // SUB
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
                case JUMP_ACCUMULATOR:
                    programCounter = (int) accumulator;
                    break;
                case HALT:  // HRS
                    finished = true;
                    break;
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
            if (finished) break;
        }

        System.out.println();
        System.out.println("Accumulator: " + Long.toHexString(accumulator));
        System.out.println("Final Memory State:\n" + formatHexList(memory, labels));
    }

    private static void set(List<Long> memory, long address, long value) {
        while (!(address < memory.size())) {
            memory.add(0L);
        }

        memory.set((int) address, value);
    }

    private static String formatHexList(List<Long> list, Map<String, Long> labels) {
        var inverted = new HashMap<Long, String>();
        for (Map.Entry<String, Long> entry : labels.entrySet()) {
            inverted.put(entry.getValue(), entry.getKey());
        }

        final var maxLabelLength = inverted.values()
                .stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);

        StringJoiner joiner = new StringJoiner(",", "{", "\n}");
        for (int i = 0; i < list.size(); i++) {
            Long value = list.get(i);
            String string = Long.toString(value, 16);

            final String labelText;
            final var casted = (long) i;
            if (inverted.containsKey(casted)) {
                final var label = inverted.get(casted);
                final var paddingLength = maxLabelLength - label.length();
                labelText = " ".repeat(paddingLength) + label + " ";
            } else {
                labelText = " ".repeat(maxLabelLength + 1);
            }

            joiner.add("\n " + labelText + Integer.toString(i, 16) + ": " + string);
        }
        return joiner.toString();
    }

    private static Result<Tuple<LinkedList<Long>, Map<String, Long>>, CompileError> assemble(String content) {
        return createRootRule()
                .parse(content)
                .flatMapValue(Assembler::parse);
    }

    private static Result<Tuple<LinkedList<Long>, Map<String, Long>>, CompileError> parse(Node root) {
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
                    final var labelChildrenPreprocessed = label.findNodeList(CHILDREN).orElse(Collections.emptyList());

                    final var labelChildren = JavaStreams.fromList(labelChildrenPreprocessed)
                            .filter(value -> value.is(INSTRUCTION_TYPE))
                            .map(Assembler::getNode)
                            .into(ResultStream::new)
                            .foldResultsLeft(new JavaList<Node>(), JavaList::add);

                    if (labelChildren.isErr()) return new Err<>(labelChildren.findErr().orElse(null));
                    program.add(new Tuple<>(labelName, labelChildren.findValue().orElse(new JavaList<>()).list()));
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

        data.put("__offset__", 2L);
        final var newFirst = programCopy.get(entryIndex).<List<Node>>mapRight(right -> {
            final var list = List.of(
                    new MapNode(INSTRUCTION_TYPE)
                            .withInt(OP_CODE, LOAD_DIRECT)
                            .withString(INSTRUCTION_LABEL, STACK_POINTER_COUNTER),
                    new MapNode(INSTRUCTION_TYPE)
                            .withInt(OP_CODE, ADD)
                            .withString(INSTRUCTION_LABEL, "__offset__"),
                    new MapNode(INSTRUCTION_TYPE)
                            .withInt(OP_CODE, STORE_DIRECT)
                            .withString(INSTRUCTION_LABEL, STACK_POINTER_COUNTER)
            );
            final var copy = new ArrayList<Node>(list);
            copy.addAll(right);
            return copy;
        });

        programCopy.set(entryIndex, newFirst);

        var labels = new HashMap<String, Long>();
        labels.put(INIT, 0L);

        final var list = new ArrayList<Node>();
        set(list, 2, new MapNode(INSTRUCTION_TYPE)
                .withInt(OP_CODE, JUMP_ADDRESS)
                .withString(INSTRUCTION_LABEL, INIT));

        labels.put(STACK_POINTER_COUNTER, STACK_POINTER_ADDRESS);
        set(list, (int) STACK_POINTER_ADDRESS, 0);

        set(list, 3, new MapNode(INSTRUCTION_TYPE)
                .withInt(OP_CODE, JUMP_ADDRESS)
                .withString(INSTRUCTION_LABEL, INIT));

        set(list, 2, new MapNode(INSTRUCTION_TYPE)
                .withInt(OP_CODE, INCREMENT)
                .withString(INSTRUCTION_LABEL, STACK_POINTER_COUNTER));

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

        set(list, 3, new MapNode(INSTRUCTION_TYPE)
                .withInt(OP_CODE, JUMP_ADDRESS)
                .withString(INSTRUCTION_LABEL, MAIN));

        return JavaStreams.fromList(list)
                .map(node -> resolveLabel(labels, node))
                .into(ResultStream::new)
                .flatMapResult(Assembler::computeBinary)
                .foldResultsLeft(new LinkedList<Long>(), (longs, aLong) -> {
                    longs.add(aLong);
                    return longs;
                })
                .mapValue(value -> new Tuple<>(value, labels));
    }

    private static Result<Node, CompileError> getNode(Node instruction) {
        return instruction.findString(MNEMONIC)
                .map(mnemonic -> parseMnemonic(instruction, mnemonic))
                .orElseGet(() -> new Err<>(new CompileError("No mnemonic present", new NodeContext(instruction))));
    }

    private static Result<Node, CompileError> parseMnemonic(Node instruction, String mnemonic) {
        return resolveMnemonic(mnemonic).mapValue(opCode -> {
            final var withOpCode = instruction.withInt(OP_CODE, opCode);
            if (withOpCode.hasInteger(ADDRESS_OR_VALUE)) return withOpCode;
            return withOpCode.withInt(ADDRESS_OR_VALUE, 0);
        });
    }

    private static Result<Integer, CompileError> resolveMnemonic(String mnemonic) {
        return switch (mnemonic) {
            case "jp" -> new Ok<>(JUMP_ADDRESS);
            case "jpac" -> new Ok<>(JUMP_ACCUMULATOR);
            case "ldd" -> new Ok<>(LOAD_DIRECT);
            case "ldi" -> new Ok<>(LOAD_INDIRECT);
            case "ldv" -> new Ok<>(LOAD_VALUE);
            case "out" -> new Ok<>(OUT);
            case "halt" -> new Ok<>(HALT);
            case "stod" -> new Ok<>(STORE_DIRECT);
            case "stoi" -> new Ok<>(STORE_INDIRECT);
            case "push" -> new Ok<>(PUSH);
            case "pop" -> new Ok<>(POP);
            case "add" -> new Ok<>(ADD);
            case "ldac" -> new Ok<>(LOAD_ACCUMULATOR);
            case "sub" -> new Ok<>(SUBTRACT);
            default -> new Err<>(new CompileError("Unknown mnemonic", new StringContext(mnemonic)));
        };
    }

    private static Result<Long, CompileError> computeBinary(Node node) {
        if (node.is(INSTRUCTION_TYPE)) return createInstruction(node);

        if (node.is(DATA_TYPE)) {
            final var dataValue = node.findString(DATA_VALUE);
            if (dataValue.isEmpty())
                return new Err<>(new CompileError("No data value present", new NodeContext(node)));

            return new Ok<>(Long.parseUnsignedLong(dataValue.orElse(""), 16));
        }

        return new Err<>(new CompileError("Unknown node", new NodeContext(node)));
    }

    private static Result<Node, CompileError> resolveLabel(Map<String, Long> labels, Node node) {
        if (!node.is(INSTRUCTION_TYPE)) return new Ok<>(node);

        final var option = node.findString(INSTRUCTION_LABEL);
        if (option.isEmpty()) return new Ok<>(node);

        final var label = option.orElse("");

        final long addressOrValue;
        if (label.equals("$sp")) {
            addressOrValue = STACK_POINTER_ADDRESS;
        } else if (labels.containsKey(label)) {
            addressOrValue = labels.get(label);
        } else {
            try {
                addressOrValue = Long.parseUnsignedLong(label, 10);
            } catch (NumberFormatException e) {
                final var format = "Label '%s' not present";
                final var message = format.formatted(label);
                return new Err<>(new CompileError(message, new NodeContext(node)));
            }
        }

        return new Ok<>(node.withInt(ADDRESS_OR_VALUE, (int) addressOrValue));
    }

    private static void set(List<Node> list, int instructionAddress, long data) {
        set(list, instructionAddress, new MapNode(DATA_TYPE)
                .withString(DATA_VALUE, Long.toUnsignedString(data, 16)));
    }

    private static void set(List<Node> list, int instructionAddress, Node instruction) {
        list.add(new MapNode(INSTRUCTION_TYPE)
                .withInt(OP_CODE, INPUT_AND_STORE)
                .withInt(ADDRESS_OR_VALUE, instructionAddress));

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
        final var opCodeOption = node.findInt(OP_CODE);
        if (opCodeOption.isEmpty()) return new Err<>(new CompileError("No op code present", new NodeContext(node)));
        final var opCode = opCodeOption.orElse(0);

        final var option = node.findInt(ADDRESS_OR_VALUE);
        if (option.isEmpty())
            return new Err<>(new CompileError("No address or value present", new NodeContext(node)));

        final var addressOrValue = (long) option.orElse(0);

        if (opCode < 0x00 || opCode > 0xFF) {
            throw new IllegalArgumentException("Opcode must be an 8-bit value (0x00 to 0xFF).");
        }
        if (addressOrValue < 0 || addressOrValue > 0x00FFFFFFFFFFFFFFL) {
            throw new IllegalArgumentException("Address/Value must be a 56-bit value (0x00 to 0x00FFFFFFFFFFFFFF).");
        }

        return new Ok<>(((long) opCode << 56) | addressOrValue);
    }
}
