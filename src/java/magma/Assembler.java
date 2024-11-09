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
import magma.app.compile.lang.CASMLang;
import magma.java.JavaMap;
import magma.java.JavaStreams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
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
        return CASMLang.createRootRule()
                .parse(content)
                .mapValue(Assembler::parse);
    }

    private static Deque<Long> parse(Node root) {
        final var state = root.findNodeList("children")
                .map(children -> JavaStreams.fromList(children).foldLeft(new State(), Assembler::foldSection))
                .orElse(new State());

        List<Long> list0 = new ArrayList<>();
        list0.add(createInstruction(INPUT_AND_STORE, 2));
        list0.add(createInstruction(JUMP_ADDRESS, 0));

        final var dataState = defineData(list0, state.data.entrySet().stream().toList());
        final var dataOffset = GLOBAL_OFFSET + dataState.data.size();

        final var labelList = state.labels.entrySet().stream().toList();

        var program = new ProgramState();
        for (var entry : labelList) {
            final var name = entry.getKey();
            final var values = entry.getValue();
            program = program.defineLabel(dataOffset, name, values);
        }

        var initial = new Result0(0, dataState);
        for (Map.Entry<String, Label> entry : program.map.entrySet()) {
            final var nodes = entry.getValue().nodes;
            initial = getResult(initial, nodes, program, dataState, dataOffset);
        }

        final var completed = dataState.add(createInstruction(INPUT_AND_STORE, STACK_POINTER_ADDRESS))
                .add(dataOffset + program.size())
                .add(createInstruction(INPUT_AND_STORE, 2))
                .add(createInstruction(JUMP_ADDRESS, dataOffset));

        final var deque = new LinkedList<>(completed.binary);
        System.out.println(formatHexList(deque, ",\n"));
        return deque;
    }

    private static Result0 getResult(Result0 initial, List<Node> nodes, ProgramState program, DataState dataState, int dataOffset) {
        var current = initial;

        for (Node node : nodes) {
            final var increment = current.increment();
            final var opCode = findOpCode(node.findString(CASMLang.OP_CODE).orElse(""));

            final var option = node.findString(CASMLang.ADDRESS_OR_VALUE);
            final var instruction = computeInstruction(dataState, program, option, opCode);

            current = increment.add(createInstruction(INPUT_AND_STORE, dataOffset + initial.counter)).add(instruction);
        }

        return current;
    }

    private static long computeInstruction(
            DataState dataState,
            ProgramState programState,
            Option<String> option,
            int opCode
    ) {
        if (option.isEmpty()) return createInstruction(opCode);

        final var addressOrValueString = option.orElse("");
        final var address = findAddress(programState, dataState, addressOrValueString);
        return createInstruction(opCode, address);
    }

    private static long findAddress(ProgramState programState, DataState dataState, String addressOrValueString) {
        return dataState.data.find(addressOrValueString)
                .or(() -> programState.findLabelAddress(addressOrValueString))
                .orElse(Long.parseLong(addressOrValueString, 16));
    }

    private static DataState defineData(List<Long> list, List<Map.Entry<String, Node>> dataList) {
        var state = new DataState(list);
        for (int index = 0; index < dataList.size(); index++) {
            final var entry = dataList.get(index);
            state = defineDatum(state, index, entry.getKey(), parseDatum(entry.getValue()));
        }

        return state;
    }

    private static DataState defineDatum(
            DataState state,
            int index,
            String label,
            long datum
    ) {
        final var address = (long) GLOBAL_OFFSET + index;

        return state
                .add(createInstruction(INPUT_AND_STORE, address))
                .add(datum)
                .mapData(data -> data.put(label, address));
    }

    private static long parseDatum(Node value) {
        final var unwrapped = value.findString("value").orElse("");

        long datum;
        if (value.is(CASMLang.CHAR_TYPE)) {
            datum = unwrapped.charAt(0);
        } else if (value.is(CASMLang.NUMBER_TYPE)) {
            datum = Long.parseLong(unwrapped, 16);
        } else {
            throw new RuntimeException("Unknown value: " + value);
        }
        return datum;
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
        if (!instructionOrLabel.is("label")) return state;

        final var name = instructionOrLabel.findString("name").orElse("");
        final var children = instructionOrLabel.findNodeList("children").orElse(Collections.emptyList());

        final var labeled = state.label(name);
        return JavaStreams.fromList(children).foldLeft(labeled, State::instruct);
    }

    private static State foldData(State state, Node datum) {
        return datum.findString("name")
                .flatMap(name -> datum.findNode("value").map(value -> state.define(name, value)))
                .orElse(state);
    }

    static Result<String, IOException> readSafe(Path path) {
        try {
            return new Ok<>(Files.readString(path));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private record Result0(int counter, DataState temp) {
        public Result0 add(long instruction) {
            return new Result0(counter, temp.add(instruction));
        }

        public Result0 increment() {
            return new Result0(counter + 1, temp);
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

    private record ProgramState(Map<String, Label> map) {
        public ProgramState() {
            this(Collections.emptyMap());
        }

        private ProgramState defineLabel(int dataOffset, String name, List<Node> values) {
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

        public ProgramState defineLabel(String name, Label label) {
            final var copy = new HashMap<>(map);
            copy.put(name, label);
            return new ProgramState(copy);
        }
    }

    private record DataState(List<Long> binary, JavaMap<String, Long> data) {
        public DataState(List<Long> binary) {
            this(binary, new JavaMap<>());
        }

        public DataState mapData(Function<JavaMap<String, Long>, JavaMap<String, Long>> mapper) {
            return new DataState(binary, mapper.apply(data));
        }

        public DataState add(long instruction) {
            final var copy = new ArrayList<>(binary);
            copy.add(instruction);
            return new DataState(copy, data);
        }
    }
}
