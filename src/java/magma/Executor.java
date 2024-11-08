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
import magma.java.JavaStreams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Executor {
    public static final Path ROOT = Paths.get(".", "src", "magma");
    public static final Path TARGET = ROOT.resolve("main.casm");
    public static final int INPUT_AND_LOAD = 0x00;
    public static final int INPUT_AND_STORE = 0x10;
    public static final int LOAD = 0x01;
    public static final int STO = 0x02;
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
    public static final int DATA_OFFSET = 3;
    public static final String OP_CODE = "op-code";
    public static final String ADDRESS_OR_VALUE = "addressOrValue";

    public static void main(String[] args) {
        readAndExecute().ifPresent(error -> System.err.println(error.format(0, 0)));
    }

    static Option<ApplicationError> readAndExecute() {
        return readSafe(TARGET)
                .mapErr(ThrowableError::new)
                .mapErr(ApplicationError::new)
                .match(Executor::assembleAndExecute, Some::new);
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

        final List<Long> memory = new ArrayList<>();
        memory.add(createInstruction(INPUT_AND_STORE, 1L));

        long accumulator = 0;  // Holds current value for operations
        int programCounter = 0;

        while (programCounter < memory.size()) {
            System.out.println(formatHexList(memory));
            final long instructionUnsigned = memory.get(programCounter);

            // Decode the instruction
            int opcode = (int) ((instructionUnsigned >> 56) & 0xFF);  // First 8 bits
            long addressOrValue = instructionUnsigned & 0x00FFFFFFFFFFFFFFL;  // Remaining 56 bits

            programCounter++;  // Move to next instruction by default

            // Execute based on opcode
            switch (opcode) {
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
                        while (!(addressOrValue < memory.size())) {
                            memory.add(0L);
                        }

                        memory.set((int) addressOrValue, polled);
                        break;
                    }
                case LOAD:  // LOAD
                    accumulator = addressOrValue < memory.size() ? memory.get((int) addressOrValue) : 0;
                    break;
                case STO:  // STO
                    if (addressOrValue < memory.size()) {
                        memory.set((int) addressOrValue, accumulator);
                    } else {
                        System.err.println("Address out of bounds.");
                    }
                    break;
                case OUT:  // OUT
                    System.out.print((char) accumulator);
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
                        memory.set((int) addressOrValue, memory.get((int) addressOrValue) + 1);
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
                    return;  // Halt execution
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

        System.out.println("Final Memory State: " + memory);
        System.out.println("Final Accumulator: " + accumulator);
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
                .mapValue(Executor::parse);
    }

    private static Deque<Long> parse(Node root) {
        final var state = root.findNodeList("children")
                .map(children -> JavaStreams.fromList(children).foldLeft(new State(), Executor::foldSection))
                .orElse(new State());

        var list = new ArrayList<Long>();
        list.add(createInstruction(INPUT_AND_STORE, 2));
        list.add(createInstruction(JUMP_ADDRESS, 0));

        Map<String, Long> dataLabels = new HashMap<>();
        final var dataList = state.data.entrySet().stream().toList();
        for (int index = 0; index < dataList.size(); index++) {
            final var entry = dataList.get(index);
            final var label = entry.getKey();
            final var value = entry.getValue();

            long data;
            if (value.is("char")) {
                data = value.findString("value").orElse("").charAt(0);
            } else {
                throw new RuntimeException("Unknown value: " + value);
            }

            final var address = (long) DATA_OFFSET + index;
            list.add(createInstruction(INPUT_AND_STORE, address));
            list.add(data);

            dataLabels.put(label, address);
        }

        final var programStart = 3 + dataLabels.size();

        final var labelList = state.labels.entrySet().stream().toList();

        var programAddresses = new HashMap<String, Long>();
        var program = new ArrayList<Node>();

        for (var entry : labelList) {
            final var name = entry.getKey();
            final var values = entry.getValue();

            final var i = DATA_OFFSET + dataLabels.size() + program.size();
            programAddresses.put(name, (long) i);

            for (Node value : values) {
                if (value.is("instruction")) {
                    program.add(value);
                }
            }
        }

        for (int i = 0; i < program.size(); i++) {
            Node node = program.get(i);
            final var opCode = findOpCode(node.findString(OP_CODE).orElse(""));

            final var option = node.findString(ADDRESS_OR_VALUE);
            final long instruction;
            if (option.isPresent()) {
                final var addressOrValue = option.orElse("");
                final var address = find(dataLabels, addressOrValue)
                        .or(() -> find(programAddresses, addressOrValue))
                        .orElse(0L);

                instruction = createInstruction(opCode, address);
            } else {
                instruction = createInstruction(opCode);
            }

            list.add(createInstruction(INPUT_AND_STORE, programStart + i));
            list.add(instruction);
        }

        list.add(createInstruction(INPUT_AND_STORE, 2));
        list.add(createInstruction(JUMP_ADDRESS, programStart));

        return new LinkedList<>(list);
    }

    private static Option<Long> find(Map<String, Long> map, String key) {
        if (map.containsKey(key)) {
            return new Some<>(map.get(key));
        } else {
            return new None<>();
        }
    }

    private static int findOpCode(String instruction) {
        final var lower = instruction.toLowerCase();
        return switch (lower) {
            case "halt" -> HALT;
            case "load" -> LOAD;
            case "out" -> OUT;
            case "jmp" -> JUMP_ADDRESS;
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
            return JavaStreams.fromList(children).foldLeft(state, Executor::foldData);
        } else if (name.equals("program")) {
            return JavaStreams.fromList(children).foldLeft(state, Executor::foldProgram);
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
        return new NodeListRule("children", new StripRule(createGroupRule("section", "section ", createStatementRule())));
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
                createDataRule(),
                createComplexInstructionRule(),
                createSimpleInstructionRule(),
                createGroupRule("label", "label", statement)
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
        final var name = new StripRule(new StringRule("name"));
        final var value = new NodeRule("value", createValueRule());
        return new TypeRule("data", new FirstRule(name, "=", new StripRule(new SuffixRule(value, ";"))));
    }

    private static Rule createValueRule() {
        return new TypeRule("char", new StripRule(new PrefixRule("'", new SuffixRule(new StringRule("value"), "'"))));
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
}
