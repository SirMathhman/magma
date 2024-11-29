package magma;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.ResultStream;
import magma.api.stream.Stream;
import magma.app.assemble.Instruction;
import magma.app.assemble.Operator;
import magma.app.assemble.State;
import magma.app.compile.CompileError;
import magma.app.compile.Layout;
import magma.app.compile.Node;
import magma.app.compile.Stack;
import magma.app.error.ApplicationError;
import magma.app.error.NodeContext;
import magma.app.error.RuntimeError;
import magma.app.error.StringContext;
import magma.java.JavaList;

import java.util.*;

import static magma.app.assemble.Operator.*;
import static magma.app.compile.CASMLang.*;
import static magma.app.compile.MagmaLang.*;

public class Main {
    public static final Instruction DEFAULT_INSTRUCTION = new Instruction(Nothing, 0);
    public static final String INSTRUCTION_ADDRESS_OR_VALUE = "address-or-value";
    public static final String ROOT_TYPE = "root";
    public static final String ROOT_CHILDREN = "children";
    public static final String STACK_POINTER = "__stack-pointer__";
    public static final String SPILL = "__spill__";

    public static void main(String[] args) {
        final var source = """
                let x = 'a';
                let y = 'b';
                let z = x;
                out y;
                """;

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
        final var nodes = program.findNodeList(ROOT_CHILDREN).map(JavaList::list)
                .orElse(Collections.emptyList());

        return assemble(nodes)
                .mapValue(Main::buildBinary)
                .mapValue(Main::wrap)
                .flatMapValue(Main::run)
                .mapErr(ApplicationError::new);
    }

    private static Node mergeIntoRoot(List<Node> compiled) {
        var count = 0;
        for (Node node : compiled) {
            if (node.is(LABEL_TYPE)) {
                count += node.findNodeList(LABEL_CHILDREN).map(JavaList::size).orElse(0);
            } else {
                count += 1;
            }
        }

        final var instruct = new ArrayList<>(List.of(
                instruct(JumpByValue, "__start__"),
                data(STACK_POINTER, count + 6),
                data(SPILL, 0)
        ));

        instruct.addAll(compiled);
        return new Node(ROOT_TYPE).withNodeList0(ROOT_CHILDREN, new JavaList<>(instruct));
    }

    private static Result<List<Node>, CompileError> compile(String input) {
        return createMagmaRootRule()
                .parse(input)
                .flatMapValue(root -> compileRoot(new Stack(), root))
                .mapValue(Tuple::right);
    }

    private static Result<Tuple<Stack, List<Node>>, CompileError> compileRoot(Stack stack, Node root) {
        return root.findNodeList(ROOT_CHILDREN)
                .orElse(new JavaList<>())
                .stream()
                .filter(node -> !node.is(WHITESPACE_TYPE))
                .foldLeftToResult(new Tuple<>(stack, new JavaList<Node>()), (current, child) -> {
                    final var instructions = current.right();
                    return compileRootMember(current.left(), child).mapValue(tuple -> {
                        return tuple.mapRight(instructions::addAll);
                    });
                })
                .mapValue(result -> {
                    return result.mapRight(children -> {
                        return List.of(label("__start__", new JavaList<Node>()
                                .addAll(children)
                                .add(instruct(Halt))
                                .list()));
                    });
                });
    }

    private static Result<Tuple<Stack, JavaList<Node>>, CompileError> compileRootMember(Stack stack, Node rootMember) {
        if (rootMember.is(DECLARATION_TYPE)) {
            final var name = rootMember.findString(DECLARATION_NAME).orElse("");
            final var value = rootMember.findNode(DECLARATION_VALUE).orElse(new Node());
            return computeLayout(stack, value).flatMapValue(layout -> {
                final var defined = stack.define(name, layout);
                return createLoader(defined, value).mapValue(loader -> {
                    return initialize(loader.left(), name, new JavaList<>(), loader.right());
                });
            });
        }

        if (rootMember.is(OUT_TYPE)) {
            final var loaded = createLoader(stack, rootMember.findNode(OUT_VALUE).orElse(new Node()));
            return loaded.mapValue(loader -> {
                final var instructions = loader.right().findInstructions()
                        .orElse(new JavaList<>())
                        .add(instruct(OutFromAccumulator));

                return new Tuple<>(loader.left(), instructions);
            });
        }

        return new Err<>(new CompileError("Unknown root member", new NodeContext(rootMember)));
    }

    private static Tuple<Stack, JavaList<Node>> initialize(Stack stack, String name, JavaList<Integer> indices, Loader loader) {
        return loader.stream()
                .map(loaderChild -> initializeMultiple(stack, name, indices, loaderChild))
                .or(() -> initializeSingle(stack, name, indices, loader))
                .orElse(new Tuple<>(stack, new JavaList<>()));
    }

    private static Option<Tuple<Stack, JavaList<Node>>> initializeSingle(
            Stack stack,
            String name,
            JavaList<Integer> indices,
            Loader loader
    ) {
        return stack.moveTo(name, indices).map(result -> {
            return result.mapRight(movingInstructions -> {
                final var loadingInstructions = loader.findInstructions().orElse(new JavaList<>());
                return movingInstructions.addAll(loadingInstructions)
                        .add(instruct(StoreIndirectly, STACK_POINTER));
            });
        });
    }

    private static Tuple<Stack, JavaList<Node>> initializeMultiple(
            Stack stack,
            String name,
            JavaList<Integer> indices,
            Stream<Tuple<Integer, Loader>> children
    ) {
        return children.foldLeft(new Tuple<>(stack, new JavaList<>()), (current, child) -> {
            final var currentStack = current.left();
            final var currentInstructions = current.right();

            return initialize(currentStack, name, indices.add(child.left()), child.right())
                    .mapRight(currentInstructions::addAll);
        });
    }

    private static Result<Layout, CompileError> computeLayout(Stack stack, Node value) {
        if (value.is(INT_TYPE) || value.is(CHAR_TYPE)) {
            return new Ok<>(new SingleLayout(1));
        }

        if (value.is(TUPLE_TYPE)) {
            return value.findNodeList(TUPLE_VALUES).orElse(new JavaList<>())
                    .stream()
                    .<JavaList<Layout>, CompileError>foldLeftToResult(new JavaList<>(), (layoutJavaList, node) -> computeLayout(stack, node).mapValue(layoutJavaList::add))
                    .mapValue(MultipleLayout::new);
        }

        if (value.is(SYMBOL_TYPE)) {
            final var text = value.findString(SYMBOL_VALUE).orElse("");
            return stack.resolve(text)
                    .<Result<Layout, CompileError>>map(Ok::new)
                    .orElseGet(() -> new Err<>(new CompileError("Symbol not defined", new StringContext(text))));
        }

        return new Err<>(new CompileError("Cannot compute layout", new NodeContext(value)));
    }

    private static Result<Tuple<Stack, Loader>, CompileError> createLoader(Stack stack, Node value) {
        if (value.is(INT_TYPE)) {
            final var integer = value.findInt(INT_VALUE).orElse(0);
            return createLoader(integer).mapValue(loader -> new Tuple<>(stack, loader));
        }

        if (value.is(TUPLE_TYPE)) {
            return value.findNodeList(TUPLE_VALUES)
                    .orElse(new JavaList<>())
                    .stream()
                    .foldLeftToResult(new Tuple<>(stack, new JavaList<Loader>()),
                            (tuple, node) -> createLoader(tuple.left(), node).mapValue(
                                    tuple0 -> tuple0.mapRight(right -> tuple.right().add(right))))
                    .mapValue(tuple0 -> tuple0.mapRight(MultipleLoader::new));
        }

        if (value.is(CHAR_TYPE)) {
            final var c = value.findString(CHAR_VALUE).orElse("");
            return createLoader(c.charAt(0)).mapValue(loader -> new Tuple<>(stack, loader));
        }

        if (value.is(SYMBOL_TYPE)) {
            final var symbolValue = value.findString(SYMBOL_VALUE).orElse("");
            return stack.moveTo(symbolValue)
                    .map(tuple -> tuple.<Loader>mapRight(SingleLoader::new))
                    .<Result<Tuple<Stack, Loader>, CompileError>>map(Ok::new)
                    .orElseGet(() -> new Err<>(new CompileError("Not defined", new StringContext(symbolValue))));
        }

        return new Err<>(new CompileError("Cannot load value", new NodeContext(value)));
    }

    private static Result<Loader, CompileError> createLoader(int integer) {
        return new Ok<>(new SingleLoader(new JavaList<Node>().add(instruct(LoadFromValue, integer))));
    }

    private static Result<List<Node>, RuntimeError> assemble(List<Node> program) {
        final var labelsToAddresses = computeLabelsToAddresses(program);
        return new JavaList<>(program).stream()
                .map(value -> resolveAddressesForNode(labelsToAddresses, value))
                .into(ResultStream::new)
                .mapResult(JavaList::new)
                .foldResultsLeft(new JavaList<Node>(), JavaList::addAll)
                .mapValue(values -> {
                    return new JavaList<Node>()
                            .addAll(values);
                })
                .mapValue(JavaList::list);
    }

    private static Map<String, Integer> computeLabelsToAddresses(List<Node> program) {
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
            final var children = node.findNodeList(LABEL_CHILDREN).map(list -> list.list()).orElse(Collections.emptyList());
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
