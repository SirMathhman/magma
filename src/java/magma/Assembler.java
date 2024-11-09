package magma;

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
import magma.app.compile.rule.*;
import magma.java.JavaMap;
import magma.java.JavaStreams;

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
    public static final int ADD = 0x04;
    public static final int SUB = 0x05;
    public static final int INC = 0x06;
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
    public static final int GLOBAL_OFFSET = 4;
    public static final String OP_CODE = "op-code";
    public static final String ADDRESS_OR_VALUE = "addressOrValue";
    public static final String CHAR_TYPE = "char";
    public static final String NUMBER_TYPE = "number";
    public static final int STACK_POINTER_ADDRESS = 3;
    private static final int PUSH = 0x11;
    private static final int POP = 0x12;

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

    // Helper function to create a valid 64-bit instruction
    private static long createInstruction(int opcode, long addressOrValue) {
        if (opcode < 0x00 || opcode > 0xFF) {
            throw new IllegalArgumentException("Opcode must be an 8-bit value (0x00 to 0xFF).");
        }
        if (addressOrValue < 0 || addressOrValue > 0x00FFFFFFFFFFFFFFL) {
            throw new IllegalArgumentException("Address/Value must be a 56-bit value (0x00 to 0x00FFFFFFFFFFFFFF).");
        }

        // Shift the opcode to the top 8 bits and combine with the addressOrValue
        return ((long) opcode << 56) | addressOrValue;
    }

    private static void execute(Deque<Long> input) {
        System.out.println("Memory footprint: " + (input.size() * BYTES_PER_LONG) + " bytes");

        final var memory = new ArrayList<Long>();
        compute(memory, input);
        System.out.println();
        System.out.println("Final Memory State: " + formatHexList(memory, ", "));
    }

    private static void compute(List<Long> memory, Deque<Long> input) {
        memory.add(createInstruction(INPUT_AND_STORE, 1L));

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
                case ADD:  // ADD
                    if (addressOrValue < memory.size()) {
                        accumulator += memory.get((int) addressOrValue);
                    } else {
                        System.err.println("Address out of bounds.");
                    }
                    break;
                case SUB:  // SUB
                    if (addressOrValue < memory.size()) {
                        accumulator -= memory.get((int) addressOrValue);
                    } else {
                        System.err.println("Address out of bounds.");
                    }
                    break;
                case INC:  // INC
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

    private static String formatHexList(List<Long> list) {
        return formatHexList(list, ", ");
    }

    private static String formatHexList(List<Long> list, String delimiter) {
        return list.stream()
                .map(value -> Long.toString(value, 16))
                .collect(Collectors.joining(delimiter, "[", "]"));
    }

    private static Result<Deque<Long>, CompileError> assemble(String content) {
        return createRootRule()
                .parse(content)
                .mapValue(Assembler::parse);
    }

    private static Deque<Long> parse(Node root) {
        final var state = root.findNodeList("children")
                .map(children -> JavaStreams.fromList(children).foldLeft(new State(), Assembler::foldSection))
                .orElse(new State());

        var list = new ArrayList<Long>();
        list.add(createInstruction(INPUT_AND_STORE, 2));
        list.add(createInstruction(JUMP_ADDRESS, 0));

        final var dataLabels = defineData(state, list);
        final var dataOffset = GLOBAL_OFFSET + dataLabels.size();
        final var labelList = state.labels.entrySet().stream().toList();

        var program = new Program();
        for (var entry : labelList) {
            final var name = entry.getKey();
            final var values = entry.getValue();
            program = program.defineLabel(dataOffset, name, values);
        }

        var counter = 0;
        for (Map.Entry<String, Label> entry : program.map.entrySet()) {
            final var nodes = entry.getValue().nodes;
            for (Node node : nodes) {
                counter++;

                final var opCode = findOpCode(node.findString(OP_CODE).orElse(""));

                final var option = node.findString(ADDRESS_OR_VALUE);
                final long instruction;
                if (option.isPresent()) {
                    final var addressOrValueString = option.orElse("");
                    Program finalProgram = program;
                    final var addressOrValue = dataLabels.find(addressOrValueString)
                            .or(() -> finalProgram.findLabelAddress(addressOrValueString))
                            .orElse(Long.parseLong(addressOrValueString, 16));

                    instruction = createInstruction(opCode, addressOrValue);
                } else {
                    instruction = createInstruction(opCode);
                }

                list.add(createInstruction(INPUT_AND_STORE, dataOffset + counter));
                list.add(instruction);
            }
        }

        list.add(createInstruction(INPUT_AND_STORE, STACK_POINTER_ADDRESS));
        list.add((long) (dataOffset + program.size()));

        list.add(createInstruction(INPUT_AND_STORE, 2));
        list.add(createInstruction(JUMP_ADDRESS, dataOffset));

        System.out.println(formatHexList(list, ",\n"));

        return new LinkedList<>(list);
    }

    private static JavaMap<String, Long> defineData(State state, List<Long> list) {
        var current = new JavaMap<String, Long>();

        final var dataList = state.data.entrySet().stream().toList();
        for (int index = 0; index < dataList.size(); index++) {
            final var entry = dataList.get(index);
            current = getStringLongJavaMap(list, current, index, entry.getKey(), entry.getValue());
        }

        return current;
    }

    private static JavaMap<String, Long> getStringLongJavaMap(
            List<Long> binary,
            JavaMap<String, Long> data,
            int index,
            String label,
            Node value
    ) {
        final var unwrapped = value.findString("value").orElse("");

        long datum;
        if (value.is(CHAR_TYPE)) {
            datum = unwrapped.charAt(0);
        } else if (value.is(NUMBER_TYPE)) {
            datum = Long.parseLong(unwrapped, 16);
        } else {
            throw new RuntimeException("Unknown value: " + value);
        }

        final var address = (long) GLOBAL_OFFSET + index;
        binary.add(createInstruction(INPUT_AND_STORE, address));
        binary.add(datum);

        return data.put(label, address);
    }

    private static int findOpCode(String instruction) {
        final var lower = instruction.toLowerCase();
        return switch (lower) {
            case "halt" -> HALT;
            case "load" -> LOAD;
            case "out" -> OUT;
            case "jmp" -> JUMP_ADDRESS;
            case "add" -> ADD;
            case "push" -> PUSH;
            case "pop" -> POP;
            default -> throw new RuntimeException("Invalid instruction: " + instruction);
        };
    }

    private static long createInstruction(int opCode) {
        return createInstruction(opCode, 0);
    }

    private static State foldSection(State state, Node node) {
        final var name = node.findString("name").orElse("");
        final var children = node.findNodeList("children").orElse(Collections.emptyList());

        if (name.equals("data")) {
            return JavaStreams.fromList(children).foldLeft(state, Assembler::foldData);
        } else if (name.equals("program")) {
            return JavaStreams.fromList(children).foldLeft(state, Assembler::foldProgram);
        } else {
            throw new RuntimeException("Unknown name: " + name);
        }
    }

    private static State foldProgram(State state, Node instructionOrLabel) {
        if (instructionOrLabel.is("label")) {
            final var name = instructionOrLabel.findString("name").orElse("");
            final var children = instructionOrLabel.findNodeList("children").orElse(Collections.emptyList());

            final var labeled = state.label(name);
            return JavaStreams.fromList(children).foldLeft(labeled, State::instruct);
        }

        return state;
    }

    private static State foldData(State state, Node datum) {
        return datum.findString("name")
                .flatMap(name -> datum.findNode("value").map(value -> state.define(name, value)))
                .orElse(state);
    }

    private static Rule createRootRule() {
        final var label = createGroupRule("label", "label ", createStatementRule());
        final var section = createGroupRule("section", "section ", new OrRule(List.of(
                new EmptyRule(),
                createDataRule(),
                label
        )));

        return new NodeListRule("children", new StripRule(section));
    }

    private static Rule createGroupRule(String type, String prefix, Rule statement) {
        final var name = new StripRule(new StringRule("name"));
        final var children = new NodeListRule("children", new StripRule(statement));
        return new TypeRule(type, new PrefixRule(prefix, new FirstRule(name, "{", new SuffixRule(children, "}"))));
    }

    private static Rule createStatementRule() {
        final var statement = new LazyRule();
        statement.setRule(new OrRule(List.of(
                new EmptyRule(),
                createComplexInstructionRule(),
                createSimpleInstructionRule()
        )));
        return statement;
    }

    private static Rule createSimpleInstructionRule() {
        final var operation = new StringRule(OP_CODE);
        return new TypeRule("instruction", new StripRule(new SuffixRule(new FilterRule(new SymbolFilter(), operation), ";")));
    }

    private static Rule createComplexInstructionRule() {
        final var operation = new StripRule(new StringRule(OP_CODE));
        final var address = new StripRule(new StringRule(ADDRESS_OR_VALUE));
        return new TypeRule("instruction", new StripRule(new FirstRule(operation, " ", new StripRule(new SuffixRule(address, ";")))));
    }

    private static Rule createDataRule() {
        final var name = new StripRule(new FilterRule(new SymbolFilter(), new StringRule("name")));
        final var value = new NodeRule("value", createValueRule());

        return new TypeRule("data", new FirstRule(name, "=", new StripRule(new SuffixRule(value, ";"))));
    }

    private static Rule createValueRule() {
        return new OrRule(List.of(
                createCharRule(),
                new TypeRule(NUMBER_TYPE, new StripRule(new FilterRule(new NumberFilter(), new StringRule("value"))))
        ));
    }

    private static TypeRule createCharRule() {
        return new TypeRule(CHAR_TYPE, new StripRule(new PrefixRule("'", new SuffixRule(new StringRule("value"), "'"))));
    }


    static Result<String, IOException> readSafe(Path path) {
        try {
            return new Ok<>(Files.readString(path));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private record State(Map<String, Node> data, Map<String, List<Node>> labels, String currentLabel) {
        public State() {
            this(Collections.emptyMap(), Collections.emptyMap(), "");
        }

        public State define(String name, Node value) {
            final var copy = new HashMap<>(data);
            copy.put(name, value);
            return new State(new HashMap<>(copy), labels, currentLabel);
        }

        public State instruct(Node instruction) {
            if (labels.containsKey(currentLabel)) {
                final var list = new ArrayList<>(labels.get(currentLabel));
                list.add(instruction);

                final var copy = new HashMap<>(labels);
                copy.put(currentLabel, list);

                return new State(data, copy, currentLabel);
            } else {
                final var copy = new HashMap<>(labels);
                copy.put(currentLabel, Collections.singletonList(instruction));
                return new State(data, copy, currentLabel);
            }
        }

        public State label(String name) {
            return new State(data, labels, name);
        }
    }

    private record Label(long location, List<Node> nodes) {
        public Label(long location) {
            this(location, Collections.emptyList());
        }

        public int size() {
            return nodes.size();
        }

        public Label add(Node node) {
            final var copy = new ArrayList<>(nodes);
            copy.add(node);
            return new Label(location, copy);
        }
    }

    private record Program(Map<String, Label> map) {
        public Program() {
            this(Collections.emptyMap());
        }

        private Program defineLabel(int dataOffset, String name, List<Node> values) {
            final var programSize = size();
            final var location = dataOffset + programSize;

            var label = new Label(location);
            for (Node value : values) {
                if (value.is("instruction")) {
                    label = label.add(value);
                }
            }

            return defineLabel(name, label);
        }

        private Option<Long> findLabelAddress(String name) {
            return new JavaMap<>(map)
                    .find(name)
                    .map(label -> label.location);
        }

        private int size() {
            return map().values()
                    .stream()
                    .mapToInt(Label::size)
                    .sum();
        }

        public Program defineLabel(String name, Label label) {
            final var copy = new HashMap<>(map);
            copy.put(name, label);
            return new Program(copy);
        }
    }
}
