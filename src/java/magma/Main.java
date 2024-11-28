package magma;

import magma.assemble.Instruction;
import magma.assemble.Operator;
import magma.assemble.State;
import magma.error.*;
import magma.error.Error;
import magma.java.JavaList;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.rule.*;
import magma.stream.ResultStream;
import magma.stream.Streams;

import java.util.*;

import static magma.assemble.Operator.*;

public class Main {
    public static final Instruction DEFAULT_INSTRUCTION = new Instruction(Nothing, 0);
    public static final String DATA_TYPE = "data";
    public static final String DATA_LABEL = "label";
    public static final String DATA_VALUE = "value";
    public static final String LABEL_TYPE = "label";
    public static final String LABEL_NAME = "name";
    public static final String LABEL_CHILDREN = "children";
    public static final String INSTRUCTION_TYPE = "instruction";
    public static final String INSTRUCTION_OPERATOR = "operator";
    public static final String INSTRUCTION_LABEL = "label";
    public static final String INSTRUCTION_ADDRESS_OR_VALUE = "address-or-value";
    public static final String ROOT_TYPE = "root";
    public static final String ROOT_CHILDREN = "children";
    public static final String STACK_POINTER = "__stack-pointer__";
    public static final String SPILL = "__spill__";

    public static void main(String[] args) {
        final var source = "let x = 5;";

        compile(source)
                .mapValue(Main::mergeIntoRoot)
                .mapErr(ApplicationError::new)
                .flatMapValue(Main::runProgram)
                .consume(
                        value -> System.out.println(value.display()),
                        error -> System.err.println(error.display())
                );
    }

    private static Result<State, ApplicationError> runProgram(Node program) {
        final var nodes = program.getListOption(ROOT_CHILDREN).map(list -> list.list())
                .orElse(Collections.emptyList());

        return assemble(nodes)
                .mapValue(Main::buildBinary)
                .mapValue(Main::wrap)
                .flatMapValue(Main::run)
                .mapErr(ApplicationError::new);
    }

    private static Node mergeIntoRoot(List<Node> compiled) {
        final var instruct = new ArrayList<>(List.of(
                instruct(JumpByValue, "__start__"),
                data(STACK_POINTER, 6),
                data(SPILL, 0)
        ));

        instruct.addAll(compiled);
        return new Node(ROOT_TYPE).withNodeList0(ROOT_CHILDREN, new JavaList<>(instruct));
    }

    private static Rule createMagmaRootRule() {
        return new SplitRule("children", createDeclarationRule());
    }

    private static TypeRule createDeclarationRule() {
        final var name = new StripRule(new StringRule("name"));
        final var value = new StripRule(new StringRule("value"));

        return new TypeRule("declaration", new FirstRule(new PrefixRule("let ", name), "=", new SuffixRule(value, ";")));
    }

    private static Result<List<Node>, CompileError> compile(String input) {
        return createMagmaRootRule()
                .parse(input)
                .mapValue(root -> {
                    return new ArrayList<>();
                });
    }

    private static Result<Node, Error> compileRootSegment(String segment) {
        if (segment.contains("=")) {

        }

        return new Err<>(new CompileError("Invalid root member", new StringContext(segment)));
    }

    private static Node instruct(Operator operator, int value) {
        return instruct(operator).withInt(INSTRUCTION_ADDRESS_OR_VALUE, value);
    }

    private static Result<List<Node>, RuntimeError> assemble(List<Node> program) {
        final var labelsToAddresses = computeLabelsToAddresses(program);
        return Streams.from(program)
                .map(value -> resolveAddressesForNode(labelsToAddresses, value))
                .into(ResultStream::new)
                .foldResultsLeft(new ArrayList<>(), (nodes, nodes2) -> {
                    nodes.addAll(nodes2);
                    return nodes;
                });
    }

    private static HashMap<String, Integer> computeLabelsToAddresses(List<Node> program) {
        var labelToAddress = new HashMap<String, Integer>();
        var address = 3;
        for (final Node node : program) {
            if (node.is(DATA_TYPE)) {
                final var labelOption = node.findString(DATA_LABEL);
                int finalAddress = address;
                labelOption.ifPresent(label -> labelToAddress.put(label, finalAddress));
                address++;
            } else if (node.is(LABEL_TYPE)) {
                final var name = node.findString(LABEL_NAME).orElse("");
                final var children = node.getListOption(LABEL_CHILDREN).map(list -> list.list()).orElse(new ArrayList<>());
                labelToAddress.put(name, address);
                address += children.size();
            } else {
                address++;
            }
        }
        return labelToAddress;
    }

    private static List<Integer> buildBinary(List<Node> nodes) {
        var list = new ArrayList<Integer>();
        for (Node node : nodes) {
            if (node.is(INSTRUCTION_TYPE)) {
                final var operator = node.findInt(INSTRUCTION_OPERATOR)
                        .flatMap(Operator::find)
                        .orElse(Nothing);

                list.add(node.findInt(INSTRUCTION_ADDRESS_OR_VALUE)
                        .map(operator::of)
                        .orElseGet(operator::empty));
            } else if (node.is(DATA_TYPE)) {
                list.add(node.findInt(DATA_VALUE).orElse(0));
            }
        }

        return list;
    }

    private static Result<List<Node>, RuntimeError> resolveAddressesForNode(Map<String, Integer> labelToAddress, Node node) {
        if (node.is(INSTRUCTION_TYPE)) {
            return resolveInstruction(labelToAddress, node).mapValue(Collections::singletonList);
        } else if (node.is(LABEL_TYPE)) {
            final var children = node.getListOption(LABEL_CHILDREN).map(list -> list.list()).orElse(Collections.emptyList());
            return children.stream()
                    .map(child -> resolveInstruction(labelToAddress, child))
                    .<Result<List<Node>, RuntimeError>>reduce(new Ok<>(new ArrayList<Node>()), (current, next) -> current.and(() -> next).mapValue(tuple -> {
                        tuple.left().add(tuple.right());
                        return tuple.left();
                    }), (_, next) -> next);
        } else {
            return new Ok<>(Collections.singletonList(node));
        }
    }

    private static Result<Node, RuntimeError> resolveInstruction(Map<String, Integer> labelToAddress, Node node) {
        final var labelOption = node.findString(INSTRUCTION_LABEL);
        if (labelOption.isEmpty()) return new Ok<>(node);

        final var label = labelOption.orElse("");
        if (labelToAddress.containsKey(label)) {
            final var addressOrValue = labelToAddress.get(label);
            return new Ok<>(node.withInt(INSTRUCTION_ADDRESS_OR_VALUE, addressOrValue));
        } else {
            return new Err<>(new RuntimeError("Label '" + label + "' not present."));
        }
    }

    private static Node label(String name, List<Node> children) {
        return new Node(LABEL_TYPE).withString(LABEL_NAME, name).withNodeList0(LABEL_CHILDREN, new JavaList<>(children));
    }

    private static Node instruct(Operator operator, String label) {
        return instruct(operator).withString(INSTRUCTION_LABEL, label);
    }

    private static Node instruct(Operator operator) {
        return new Node(INSTRUCTION_TYPE).withInt(INSTRUCTION_OPERATOR, operator.computeOpCode());
    }

    private static Node data(String label, int value) {
        return new Node(DATA_TYPE)
                .withString(DATA_LABEL, label)
                .withInt(DATA_VALUE, value);
    }

    private static Deque<Integer> wrap(List<Integer> instructions) {
        var input = new LinkedList<>(List.of(
                InAddress.of(2),
                JumpByValue.of(0)
        ));

        for (int i = 0; i < instructions.size(); i++) {
            int instruction = instructions.get(i);
            input.add(InAddress.of(3 + i));
            input.add(instruction);
        }

        input.addAll(List.of(
                InAddress.of(2),
                JumpByValue.of(3)
        ));

        return input;
    }

    private static Result<State, RuntimeError> run(Deque<Integer> input) {
        var state = new State();
        while (true) {
            final var instructionOption = state.findCurrentInstruction();
            if (instructionOption.isEmpty()) break;
            final var instruction = instructionOption.orElse(DEFAULT_INSTRUCTION);

            final var processedResult = process(state.next(), input, instruction);
            if (processedResult.isErr()) return processedResult.preserveErr(state);

            final var processedState = processedResult.findValue().orElse(new None<>());
            if (processedState.isEmpty()) break;
            state = processedState.orElse(state);
        }

        return new Ok<>(state);
    }

    private static Result<Option<State>, RuntimeError> process(State state, Deque<Integer> input, Instruction instruction) {
        final var addressOrValue = instruction.addressOrValue();
        return switch (instruction.operator()) {
            case InAddress -> handleInAddress(state, input, instruction);
            case JumpByValue -> new Ok<>(new Some<>(state.jumpByValue(addressOrValue)));
            case JumpByAddress -> handleJumpByAddress(state, addressOrValue);
            case Nothing -> new Ok<>(new Some<>(state));
            case Halt -> new Ok<>(new None<>());
            case OutFromValue -> {
                System.out.print((char) addressOrValue);
                yield new Ok<>(new Some<>(state));
            }
            case OutFromAccumulator -> {
                System.out.print((char) state.getAccumulator());
                yield new Ok<>(new Some<>(state));
            }
            case LoadFromAddress -> handleLoadFromAddress(state, addressOrValue);
            case AddFromAddress -> handleAddFromAddress(state, addressOrValue);
            case JumpConditionByValue -> new Ok<>(new Some<>(state.jumpConditionByValue(addressOrValue)));
            case SubtractValue -> new Ok<>(new Some<>(state.subtract(addressOrValue)));
            case Not -> new Ok<>(new Some<>(state.invert()));
            case AddFromValue -> new Ok<>(new Some<>(state.add(addressOrValue)));
            case StoreAtAddress -> new Ok<>(new Some<>(state.set(addressOrValue)));
            case LoadFromValue -> new Ok<>(new Some<>(state.loadFromValue(addressOrValue)));
        };
    }

    private static Result<Option<State>, RuntimeError> handleJumpByAddress(State state, int addressOrValue) {
        return state.jumpByAddress(addressOrValue)
                .<Result<Option<State>, RuntimeError>>map(value -> new Ok<>(new Some<>(value)))
                .orElseGet(() -> new Err<>(new RuntimeError("Invalid address: " + addressOrValue)));
    }

    private static Result<Option<State>, RuntimeError> handleAddFromAddress(State state, int addressOrValue) {
        return state.addFromAddress(addressOrValue)
                .<Result<Option<State>, RuntimeError>>map(value -> new Ok<>(new Some<>(value)))
                .orElseGet(() -> new Err<>(new RuntimeError("Invalid address: " + addressOrValue)));
    }

    private static Result<Option<State>, RuntimeError> handleLoadFromAddress(State state, int addressOrValue) {
        return state.loadFromAddress(addressOrValue)
                .<Result<Option<State>, RuntimeError>>map(value -> new Ok<>(new Some<>(value)))
                .orElseGet(() -> new Err<>(new RuntimeError("Invalid address: " + addressOrValue)));
    }

    private static Result<Option<State>, RuntimeError> handleInAddress(State state, Deque<Integer> input, Instruction instruction) {
        if (input.isEmpty()) {
            return new Err<>(new RuntimeError("Input is empty."));
        } else {
            final var copy = state.set(instruction.addressOrValue(), input.poll());
            return new Ok<>(new Some<>(copy));
        }
    }

}
