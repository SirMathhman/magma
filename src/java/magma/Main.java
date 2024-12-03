package magma;

import magma.api.error.RuntimeError;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.ResultStream;
import magma.app.ApplicationError;
import magma.app.compile.Compiler;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.lang.casm.assemble.Instruction;
import magma.app.compile.lang.casm.assemble.Operator;
import magma.app.compile.lang.casm.assemble.State;
import magma.java.JavaList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static magma.app.compile.lang.casm.CASMLang.*;
import static magma.app.compile.lang.casm.assemble.Operator.*;

public class Main {
    public static final Instruction DEFAULT_INSTRUCTION = new Instruction(Nothing, 0);
    public static final String INSTRUCTION_ADDRESS_OR_VALUE = "address-or-value";
    public static final String ROOT_TYPE = "root";

    public static void main(String[] args) {
        try {
            final var source = Files.readString(Paths.get(".", "main.mgs"));
            Compiler.compile(source)
                    .mapErr(ApplicationError::new)
                    .flatMapValue(Main::runProgram)
                    .consume(
                            value -> System.out.println(value.display()),
                            error -> System.err.println(error.display())
                    );
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static Result<State, ApplicationError> runProgram(Node program) {
        System.out.println(program.display());

        final var nodes = program.findNodeList(Compiler.ROOT_CHILDREN)
                .map(JavaList::list)
                .orElse(Collections.emptyList());

        return assemble(nodes)
                .flatMapValue((List<Node> nodes1) -> buildBinary(nodes1).mapErr(ApplicationError::new))
                .mapErr(err -> new ApplicationError(new CompileError("Unknown program", new NodeContext(program), err)))
                .mapValue(Main::wrap)
                .flatMapValue((Deque<Integer> input) -> run(input).mapErr(ApplicationError::new))
                .mapErr(ApplicationError::new);
    }

    private static Result<List<Node>, ApplicationError> assemble(List<Node> program) {
        return computeLabelsToAddresses(program)
                .mapErr(ApplicationError::new)
                .flatMapValue(labelsToAddresses -> assembleWithAddresses(program, labelsToAddresses));
    }

    private static Result<List<Node>, ApplicationError> assembleWithAddresses(List<Node> program, Map<String, Integer> labelsToAddresses) {
        return new JavaList<>(program).stream()
                .map(value -> resolveAddressesForNode(labelsToAddresses, value))
                .into(ResultStream::new)
                .mapResult(JavaList::new)
                .foldResultsLeft(new JavaList<Node>(), JavaList::addAll)
                .mapValue(JavaList::list)
                .mapErr(ApplicationError::new);
    }

    private static Result<Map<String, Integer>, CompileError> computeLabelsToAddresses(List<Node> program) {
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
                final var children = node.findNodeList(LABEL_CHILDREN)
                        .map(JavaList::list)
                        .orElse(new ArrayList<>());

                labelToAddress.put(name, address);
                address += children.size();
            } else if(node.is(INSTRUCTION_TYPE)) {
                address++;
            } else {
                return new Err<>(new CompileError("Unknown node to link", new NodeContext(node)));
            }
        }
        return new Ok<>(labelToAddress);
    }

    private static Result<List<Integer>,CompileError> buildBinary(List<Node> nodes) {
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
            } else {
                return new Err<>(new CompileError("Unknown binary value", new NodeContext(node)));
            }
        }

        return new Ok<>(list);
    }

    private static Result<List<Node>, RuntimeError> resolveAddressesForNode(Map<String, Integer> labelToAddress, Node node) {
        if (node.is(INSTRUCTION_TYPE)) {
            return resolveInstruction(labelToAddress, node).mapValue(Collections::singletonList);
        } else if (node.is(LABEL_TYPE)) {
            final var children = node.findNodeList(LABEL_CHILDREN).map(JavaList::list).orElse(Collections.emptyList());
            return children.stream()
                    .map(child -> resolveInstruction(labelToAddress, child))
                    .<Result<List<Node>, RuntimeError>>reduce(new Ok<>(new ArrayList<>()), (current, next) -> current.and(() -> next).mapValue(tuple -> {
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
            case LoadDirectly -> handleLoadFromAddress(state, addressOrValue);
            case AddFromAddress -> handleAddFromAddress(state, addressOrValue);
            case JumpConditionByValue -> new Ok<>(new Some<>(state.jumpConditionByValue(addressOrValue)));
            case SubtractFromValue -> new Ok<>(new Some<>(state.subtract(addressOrValue)));
            case Not -> new Ok<>(new Some<>(state.invert()));
            case AddFromValue -> new Ok<>(new Some<>(state.add(addressOrValue)));
            case LoadFromValue -> new Ok<>(new Some<>(state.loadFromValue(addressOrValue)));
            case StoreDirectly -> new Ok<>(new Some<>(state.storeDirectly(addressOrValue)));
            case StoreIndirectly -> new Ok<>(new Some<>(state.storeIndirectly(addressOrValue)));
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
