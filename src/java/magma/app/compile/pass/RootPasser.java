package magma.app.compile.pass;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.Passer;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.error.StringContext;
import magma.app.compile.lang.CASMLang;
import magma.app.compile.lang.MagmaLang;
import magma.java.JavaList;
import magma.java.JavaOrderedMap;

import java.util.List;

import static magma.Assembler.*;
import static magma.app.compile.lang.CASMLang.*;
import static magma.app.compile.lang.MagmaLang.NUMBER_TYPE;
import static magma.app.compile.lang.MagmaLang.ROOT_TYPE;
import static magma.app.compile.lang.MagmaLang.*;

public class RootPasser implements Passer {
    public static final String CACHE = "cache";
    public static final String PROGRAM_SECTION = SECTION_PROGRAM;
    public static final String DATA_SECTION = "data";

    private static Node instruct(String mnemonic) {
        return new MapNode(INSTRUCTION_TYPE)
                .withString(BLOCK_BEFORE_CHILD, "\n\t\t")
                .withString(MNEMONIC, mnemonic);
    }

    private static Result<Tuple<JavaOrderedMap<String, Long>, JavaList<Node>>, CompileError> parseRootMember(JavaOrderedMap<String, Long> state, Node node) {
        if (node.is(DECLARATION_TYPE)) {
            final var name = node.findString(DECLARATION_NAME).orElse("");
            final var value = node.findNode(DECLARATION_VALUE).orElse(new MapNode());

            return computeLength(value).flatMapValue(length -> {
                final var added = state.put(name, length);
                return loadValue(state, value)
                        .mapValue(loadInstructions -> {
                            final var offset = state.stream()
                                    .map(Tuple::right)
                                    .foldLeft(0L, Long::sum);

                            return loadInstructions.addAll(pushToStack(offset));
                        })
                        .mapValue(list -> new Tuple<>(added, list));
            });
        }

        if (node.is(RETURN_TYPE)) {
            final var value = node.findNode(RETURN_VALUE).orElse(new MapNode());
            return loadValue(state, value).mapValue(list -> list
                            .add(instruct("out"))
                            .add(instruct("halt")))
                    .mapValue(list -> new Tuple<>(state, list));
        }

        final var context = new NodeContext(node);
        final var message = new CompileError("Cannot create instructions for root child", context);
        return new Err<>(message);
    }

    private static JavaList<Node> pushToStack(Long offset) {
        return new JavaList<Node>()
                .addAll(moveStackPointerRight(offset))
                .add(instructStackPointer("stoi"))
                .addAll(moveStackPointerLeft(offset));
    }

    private static Result<Long, CompileError> computeLength(Node node) {
        if (node.is(ARRAY_TYPE)) {
            final var values = node.findNodeList(ARRAY_VALUES).orElse(new JavaList<>());
            return values.stream().foldLeftToResult(1L, (sum, node1) -> computeLength(node1).mapValue(length -> sum + length));
        }

        if (node.is(NUMBER_TYPE)) return new Ok<>(1L);
        if (node.is(REFERENCE_TYPE)) return new Ok<>(1L);

        return new Err<>(new CompileError("Cannot compute length", new NodeContext(node)));
    }

    private static Result<JavaList<Node>, CompileError> loadValue(JavaOrderedMap<String, Long> state, Node node) {
        return parseNumberValue(node)
                .or(() -> parseSymbolValue(state, node))
                .or(() -> parseAddValue(state, node))
                .or(() -> loadArrayValue(state, node))
                .or(() -> loadReferenceValue(state, node))
                .orElseGet(() -> createUnknownValueError(node));
    }

    private static Option<Result<JavaList<Node>, CompileError>> loadReferenceValue(JavaOrderedMap<String, Long> state, Node node) {
        if (!node.is(REFERENCE_TYPE)) return new None<>();

        final var value = node.findNode(REFERENCE_VALUE).orElse(new MapNode());
        if (!value.is(SYMBOL_TYPE))
            return new Some<>(new Err<>(new CompileError("Cannot reference", new NodeContext(value))));

        final var textValue = value.findString(SYMBOL_VALUE).orElse("");
        final var addressOption = state.find(textValue);
        if (addressOption.isEmpty())
            return new Some<>(new Err<>(new CompileError("No address present", new NodeContext(value))));
        final var address = addressOption.orElse(0L);

        return new Some<>(new Ok<>(new JavaList<Node>()
                .add(instructStackPointer("ldd"))));
    }

    private static Option<Result<JavaList<Node>, CompileError>> loadArrayValue(JavaOrderedMap<String, Long> state, Node node) {
        if (!node.is(ARRAY_TYPE)) return new None<>();

        final var values = node.findNodeList(ARRAY_VALUES).orElse(new JavaList<>());
        final var first = values.get(0).orElse(new MapNode());
        final var loaded = loadValue(state, first).findValue().orElse(new JavaList<>());

        return new Some<>(new Ok<>(new JavaList<Node>()
                .add(instructStackPointer("ldd"))
                .add(instruct("addv", 1))
                .add(instructStackPointer("stoi"))));
    }

    private static Option<Result<JavaList<Node>, CompileError>> parseAddValue(JavaOrderedMap<String, Long> state, Node node) {
        if (!node.is(ADD_TYPE)) return new None<>();

        final var leftNode = node.findNode(ADD_LEFT).orElse(new MapNode());
        final var rightNode = node.findNode(ADD_RIGHT).orElse(new MapNode());

        return new Some<>(loadValue(state, leftNode).flatMapValue(list -> {
            if (rightNode.is(NUMBER_TYPE)) {
                final var numberValue = rightNode.findInt(MagmaLang.NUMBER_VALUE).orElse(0);
                return new Ok<>(list.add(instruct("addv", numberValue)));
            } else if (rightNode.is(SYMBOL_TYPE)) {
                final var value = rightNode.findString(SYMBOL_VALUE).orElse("");
                return handleWithinSymbol(state, value, instructStackPointer("addi")).mapValue(list::addAll);
            } else {
                return new Ok<>(list);
            }
        }));
    }

    private static Err<JavaList<Node>, CompileError> createUnknownValueError(Node node) {
        final var context = new NodeContext(node);
        final var error = new CompileError("Unknown value present", context);
        return new Err<>(error);
    }

    private static Option<Result<JavaList<Node>, CompileError>> parseNumberValue(Node node) {
        if (!node.is(NUMBER_TYPE)) return new None<>();

        final var value = node.findInt(MagmaLang.NUMBER_VALUE).orElse(0);
        final var instructions = new JavaList<Node>().add(instruct("ldv", value));
        return new Some<>(new Ok<>(instructions));
    }

    private static Option<Result<JavaList<Node>, CompileError>> parseSymbolValue(
            JavaOrderedMap<String, Long> state,
            Node node
    ) {
        if (!node.is(SYMBOL_TYPE)) return new None<>();

        final var value = node.findString(SYMBOL_VALUE).orElse("");
        return new Some<>(handleWithinSymbol(state, value, instructStackPointer("ldi")));
    }

    private static Result<JavaList<Node>, CompileError> handleWithinSymbol(JavaOrderedMap<String, Long> state, String value, Node instruction) {
        final var indexOption = state.findIndexOfKey(value);
        if (indexOption.isEmpty()) {
            final var context = new StringContext(value);
            final var error = new CompileError("Symbol not defined", context);
            return new Err<>(error);
        }

        final var index = indexOption.orElse(0);
        final var map = state.sliceToIndex(index).orElse(new JavaOrderedMap<>());
        final var addressesToShift = map.stream().map(Tuple::right).foldLeft(0L, Long::sum);

        final var instructions = new JavaList<Node>()
                .addAll(moveStackPointerRight(addressesToShift))
                .add(instruction)
                .addAll(moveStackPointerLeft(addressesToShift));

        return new Ok<>(instructions);
    }

    private static Node instruct(String mnemonic, String label) {
        return instruct(mnemonic).withString(INSTRUCTION_LABEL, label);
    }

    private static JavaList<Node> moveStackPointerLeft(long addressesToShift) {
        return moveStackPointer("subv", addressesToShift);
    }

    private static JavaList<Node> moveStackPointer(String instruction, long addressesToShift) {
        if (addressesToShift == 0) return new JavaList<>();
        return new JavaList<Node>()
                .add(instruct("stod", CACHE))
                .add(instructStackPointer("ldd"))
                .add(instruct(instruction, addressesToShift))
                .add(instructStackPointer("stod"))
                .add(instruct("ldd", CACHE));
    }

    private static JavaList<Node> moveStackPointerRight(long addressesToShift) {
        return moveStackPointer("addv", addressesToShift);
    }

    private static Node instructStackPointer(String mnemonic) {
        return instruct(mnemonic, STACK_POINTER);
    }

    private static Node instruct(String mnemonic, long addressOrValue) {
        return instruct(mnemonic).withInt(INSTRUCTION_ADDRESS_OR_VALUE, (int) addressOrValue);
    }

    private static Ok<Tuple<State, Node>, CompileError> wrapInstructions(State state, JavaList<Node> instructions) {
        final var labelValue = new MapNode(BLOCK_TYPE)
                .withNodeList(CHILDREN, instructions.list())
                .withString(BLOCK_AFTER_CHILDREN, "\n\t");

        final var label = new MapNode(LABEL_TYPE)
                .withString(GROUP_NAME, MAIN)
                .withString(GROUP_AFTER_NAME, " ")
                .withString(BLOCK_BEFORE_CHILD, "\n\t")
                .withNode(GROUP_VALUE, labelValue);

        final var programValue = new MapNode(BLOCK_TYPE)
                .withNodeList(CHILDREN, List.of(label))
                .withString(BLOCK_AFTER_CHILDREN, "\n");

        final var programSection = new MapNode(SECTION_TYPE)
                .withString(GROUP_NAME, PROGRAM_SECTION)
                .withString(GROUP_AFTER_NAME, " ")
                .withNode(GROUP_VALUE, programValue);

        final var cacheValue = new MapNode(CASMLang.NUMBER_TYPE)
                .withString(CASMLang.NUMBER_VALUE, "0");

        final var cache = new MapNode(DATA_TYPE)
                .withString(DATA_NAME, "cache")
                .withNode(DATA_VALUE, cacheValue);

        final var dataValue = new MapNode(BLOCK_TYPE)
                .withNodeList(CHILDREN, List.of(cache));

        final var dataSection = new MapNode(SECTION_TYPE)
                .withString(GROUP_NAME, DATA_SECTION)
                .withString(GROUP_AFTER_NAME, " ")
                .withNode(GROUP_VALUE, dataValue);

        final var node = new MapNode(ROOT_TYPE)
                .withNodeList(CHILDREN, List.of(dataSection, programSection));

        return new Ok<>(new Tuple<>(state, node));
    }

    private static Result<Tuple<JavaOrderedMap<String, Long>, JavaList<Node>>, CompileError> foldRootMember(
            Tuple<JavaOrderedMap<String, Long>, JavaList<Node>> tuple,
            Node node
    ) {
        final var oldState = tuple.left();
        final var oldList = tuple.right();

        return parseRootMember(oldState, node)
                .mapValue(newStateAndInstructions -> newStateAndInstructions.mapRight(oldList::addAll));
    }

    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        if (!node.is(ROOT_TYPE)) return new None<>();
        return new Some<>(passImpl(state, node));
    }

    private Result<Tuple<State, Node>, CompileError> passImpl(State state, Node node) {
        final var childrenOption = node.findNodeList(CHILDREN);
        if (childrenOption.isEmpty()) {
            final var context = new NodeContext(node);
            final var error = new CompileError("No root children present", context);
            return new Err<>(error);
        }

        return childrenOption.orElse(new JavaList<Node>())
                .stream()
                .foldLeftToResult(new Tuple<>(new JavaOrderedMap<>(), new JavaList<>()), RootPasser::foldRootMember)
                .mapValue(Tuple::right)
                .flatMapValue(instructions -> wrapInstructions(state, instructions));
    }
}
