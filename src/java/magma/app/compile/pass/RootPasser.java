package magma.app.compile.pass;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.compile.*;
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
import static magma.app.compile.lang.MagmaLang.ROOT_TYPE;
import static magma.app.compile.lang.MagmaLang.*;

public class RootPasser implements Passer {
    public static final String PROGRAM_SECTION = SECTION_PROGRAM;
    public static final String DATA_SECTION = "data";
    public static final String DATA_CACHE = "cache";
    public static final String TYPE_LENGTH = "length";

    private static Result<Tuple<Stack, JavaList<Node>>, CompileError> writeRootMember(Stack definitions, Node node) {
        return writeDeclaration(definitions, node)
                .or(() -> writeReturn(definitions, node))
                .orElseGet(() -> invalidateRootMember(node));
    }

    private static Option<Result<Tuple<Stack, JavaList<Node>>, CompileError>> writeReturn(Stack definitions, Node node) {
        if (!node.is(RETURN_TYPE)) return new None<>();

        final var returnValue = node.findNode(RETURN_VALUE).orElse(new MapNode());
        return new Some<>(loadValue(definitions, returnValue)
                .mapValue(list -> list.addAll(new JavaList<Node>()
                        .addLast(instruct("out"))
                        .addLast(instruct("halt"))))
                .mapValue(instructions -> new Tuple<>(definitions, instructions)));
    }

    private static Option<Result<Tuple<Stack, JavaList<Node>>, CompileError>> writeDeclaration(Stack stack, Node node) {
        if (!node.is(DECLARATION_TYPE)) return new None<>();

        final var name = node.findString(DECLARATION_NAME).orElse("");
        final var value = node.findNode(DECLARATION_VALUE).orElse(new MapNode());

        return new Some<>(loadValue(stack, value)
                .flatMapValue(instructions -> formatInstructions(stack, instructions.addFirst(comment("load value of declaration '" + name + "'"))))
                .flatMapValue(instructions -> resolveType(stack, value)
                        .mapValue(type -> stack.define(name, type))
                        .mapValue(newStack -> new Tuple<>(newStack, instructions))));
    }

    private static Result<JavaList<Node>, CompileError> formatInstructions(Stack stack, JavaList<Node> instructions) {
        return stack.computeCurrentFrameSize().mapValue(frameSize -> new JavaList<Node>()
                .addAll(moveStackPointerRight(frameSize))
                .addAll(instructions)
                .addLast(instructStackPointer("stoi"))
                .addAll(moveStackPointerLeft(frameSize)));
    }

    private static Result<Node, CompileError> resolveType(Stack stack, Node type) {
        if (type.is(NUMBER_TYPE)) {
            return new Ok<>(new MapNode("primitive")
                    .withString("sign", "false")
                    .withInt("bits", 64)
                    .withInt(TYPE_LENGTH, 1));
        }
        if (type.is(TUPLE_TYPE)) {
            return type.findNodeList(TUPLE_VALUES).orElse(new JavaList<>()).stream().foldLeftToResult(new JavaList<Node>(), (list, child) -> resolveType(stack, child).mapValue(list::addLast))
                    .mapValue(values -> {
                        final var length = values.stream()
                                .map(value -> value.findInt(TYPE_LENGTH))
                                .flatMap(Streams::fromOption)
                                .foldLeft(1, Integer::sum);

                        return new MapNode("tuple")
                                .withNodeList(TUPLE_VALUES, values)
                                .withInt(TYPE_LENGTH, length);
                    });
        }
        if (type.is(SYMBOL_TYPE)) {
            final var symbol = type.findString(SYMBOL_VALUE).orElse("");
            return stack.find(symbol)
                    .<Result<Node, CompileError>>map(Ok::new)
                    .orElseGet(() -> new Err<>(new CompileError("Symbol not defined", new StringContext(symbol))));
        }

        return new Err<>(new CompileError("Unknown type", new NodeContext(type)));
    }

    private static Result<Long, CompileError> computeLength(Node value) {
        if (value.is(NUMBER_TYPE)) return new Ok<>(1L);
        if (value.is(TUPLE_TYPE)) return value.findNodeList(TUPLE_VALUES).orElse(new JavaList<>())
                .stream()
                .map(RootPasser::computeLength)
                .into(ResultStream::new)
                .foldResultsLeft(1L, Long::sum);

        return new Err<>(new CompileError("Unknown value to compute length", new NodeContext(value)));
    }

    private static JavaList<Node> moveStackPointerLeft(long offset) {
        if (offset == 0) return new JavaList<>();
        return new JavaList<Node>()
                .addLast(comment("move stack pointer left " + offset))
                .addAll(moveStackPointer(instruct("subv", offset)));
    }

    private static JavaList<Node> moveStackPointerRight(long offset) {
        if (offset == 0) return new JavaList<>();
        return new JavaList<Node>()
                .addLast(comment("move stack pointer right " + offset))
                .addAll(moveStackPointer(instruct("addv", offset)));
    }

    private static Node comment(String message) {
        return new MapNode(COMMENT_TYPE)
                .withString(BLOCK_BEFORE_CHILD, "\n\t\t")
                .withString(COMMENT_VALUE, message);
    }

    private static JavaList<Node> moveStackPointer(Node instruction) {
        return new JavaList<Node>()
                .addLast(instruct("stod", DATA_CACHE))
                .addLast(instructStackPointer("ldd"))
                .addLast(instruction)
                .addLast(instructStackPointer("stod"))
                .addLast(instruct("ldd", DATA_CACHE));
    }

    private static Node instruct(String mnemonic, long addressOrValue) {
        return instruct(mnemonic).withInt(INSTRUCTION_ADDRESS_OR_VALUE, Math.toIntExact(addressOrValue));
    }

    private static Node instruct(String mnemonic, String label) {
        return instruct(mnemonic).withString(INSTRUCTION_LABEL, label);
    }

    private static Node instruct(String mnemonic) {
        return new MapNode(INSTRUCTION_TYPE)
                .withString(BLOCK_BEFORE_CHILD, "\n\t\t")
                .withString(INSTRUCTION_MNEMONIC, mnemonic);
    }

    private static Result<JavaList<Node>, CompileError> loadValue(Stack definitions, Node node) {
        return loadNumber(node)
                .or(() -> loadSymbol(definitions, node))
                .or(() -> loadTuple(definitions, node))
                .or(() -> loadIndex(definitions, node))
                .orElseGet(() -> new Err<>(new CompileError("Unknown value", new NodeContext(node))));
    }

    private static Option<Result<JavaList<Node>, CompileError>> loadIndex(Stack definitions, Node node) {
        if (!node.is(INDEX_TYPE)) return new None<>();

        final var value = node.findNode(INDEX_VALUE).orElse(new MapNode());
        final var offset = node.findNode(INDEX_OFFSET).orElse(new MapNode());

        return new Some<>(loadValue(definitions, value).flatMapValue(loadedValue -> {
            return loadValue(definitions, offset).mapValue(loadedOffset -> {
                return loadedValue
                        .addLast(comment("load index"))
                        .addLast(instruct("stod", DATA_CACHE))
                        .addAll(loadedOffset)
                        .addLast(instruct("addd", DATA_CACHE))
                        .addLast(instruct("stod", DATA_CACHE))
                        .addLast(instruct("ldi", DATA_CACHE));
            });
        }));
    }

    private static Option<Result<JavaList<Node>, CompileError>> loadTuple(Stack definitions, Node node) {
        if (!node.is(TUPLE_TYPE)) return new None<>();

        final var values = node.findNodeList(TUPLE_VALUES).orElse(new JavaList<>());
        final var list = values.streamWithIndex()
                .foldLeftToResult(new JavaList<Node>().addAll(moveStackPointerRight(1)), (current, tuple) -> {
                    final var index = tuple.left();
                    final var value = tuple.right();

                    return computeLength(value).flatMapValue(valueLength -> loadValue(definitions, value).mapValue(instructions -> {
                        final var stored = new JavaList<Node>()
                                .addLast(comment("load tuple element index " + index))
                                .addAll(instructions)
                                .addLast(instructStackPointer("stoi"));

                        if (index == values.size() - 1) {
                            return stored;
                        } else {
                            return stored.addAll(moveStackPointerRight(valueLength));
                        }
                    }).mapValue(current::addAll));
                })
                .flatMapValue(instructions -> {
                    return values.stream()
                            .map(RootPasser::computeLength)
                            .into(ResultStream::new)
                            .foldResultsLeft(-1L, Long::sum).mapValue(sum -> instructions
                                    .addAll(moveStackPointerLeft(sum))
                                    .addLast(comment("store tuple head"))
                                    .addLast(instructStackPointer("ldd"))
                                    .addAll(moveStackPointerLeft(1)));
                });

        return new Some<>(list);
    }

    private static Option<Result<JavaList<Node>, CompileError>> loadSymbol(Stack definitions, Node node) {
        if (!node.is(SYMBOL_TYPE)) return new None<>();

        final var value = node.findString(SYMBOL_VALUE).orElse("");
        return new Some<>(definitions.computeFrameSizeToSymbol(value).mapValue(offset -> new JavaList<Node>()
                .addAll(moveStackPointerRight(offset))
                .addLast(instructStackPointer("ldi"))
                .addAll(moveStackPointerLeft(offset))));
    }

    private static Node instructStackPointer(String mnemonic) {
        return instruct(mnemonic, STACK_POINTER);
    }

    private static Option<Result<JavaList<Node>, CompileError>> loadNumber(Node node) {
        if (!node.is(MagmaLang.NUMERIC_TYPE)) return new None<>();

        final var value = node.findInt(NUMERIC_VALUE).orElse(0);
        final var instructions = new JavaList<Node>()
                .addLast(comment("load number " + value))
                .addLast(instruct("ldv").withInt(INSTRUCTION_ADDRESS_OR_VALUE, value));

        return new Some<>(new Ok<>(instructions));
    }

    private static Result<Tuple<Stack, JavaList<Node>>, CompileError> invalidateRootMember(Node node) {
        final var context = new NodeContext(node);
        final var message = new CompileError("Cannot create instructions for root child", context);
        return new Err<>(message);
    }

    private static Ok<Tuple<State, Node>, CompileError> wrapInstructions(State state, JavaList<Node> instructions) {
        Node node4 = new MapNode(BLOCK_TYPE);
        final var labelValue = node4.withNodeList(CHILDREN, new JavaList<>(instructions.list()))
                .withString(BLOCK_AFTER_CHILDREN, "\n\t");

        final var label = new MapNode(LABEL_TYPE)
                .withString(GROUP_NAME, MAIN)
                .withString(GROUP_AFTER_NAME, " ")
                .withString(BLOCK_BEFORE_CHILD, "\n\t")
                .withNode(GROUP_VALUE, labelValue);

        Node node3 = new MapNode(BLOCK_TYPE);
        final var programValue = node3.withNodeList(CHILDREN, new JavaList<>(List.of(label)))
                .withString(BLOCK_AFTER_CHILDREN, "\n");

        final var programSection = new MapNode(SECTION_TYPE)
                .withString(GROUP_NAME, PROGRAM_SECTION)
                .withString(GROUP_AFTER_NAME, " ")
                .withNode(GROUP_VALUE, programValue);

        final var cacheValue = new MapNode(CASMLang.NUMBER_TYPE)
                .withString(CASMLang.NUMBER_VALUE, "0");

        final var cache = new MapNode(DATA_TYPE)
                .withString(BLOCK_BEFORE_CHILD, "\n\t")
                .withString(DATA_AFTER_NAME, " ")
                .withString(DATA_BEFORE_VALUE, " ")
                .withString(DATA_NAME, DATA_CACHE)
                .withNode(DATA_VALUE, cacheValue);

        var node2 = new MapNode(BLOCK_TYPE);
        final var dataValue = node2.withNodeList(CHILDREN, new JavaList<>(List.of(cache)))
                .withString(BLOCK_AFTER_CHILDREN, "\n");

        final var dataSection = new MapNode(SECTION_TYPE)
                .withString(GROUP_NAME, DATA_SECTION)
                .withString(GROUP_AFTER_NAME, " ")
                .withString(GROUP_AFTER, "\n")
                .withNode(GROUP_VALUE, dataValue);

        Node node1 = new MapNode(ROOT_TYPE);
        final var node = node1.withNodeList(CHILDREN, new JavaList<>(List.of(dataSection, programSection)));

        return new Ok<>(new Tuple<>(state, node));
    }

    private static Result<Tuple<Stack, JavaList<Node>>, CompileError> foldRootMember(
            Tuple<Stack, JavaList<Node>> tuple,
            Node node
    ) {
        final var oldState = tuple.left();
        final var oldList = tuple.right();

        return writeRootMember(oldState, node)
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

        return childrenOption.orElse(new JavaList<>())
                .stream()
                .foldLeftToResult(new Tuple<>(new Stack(), new JavaList<>()), RootPasser::foldRootMember)
                .mapValue(Tuple::right)
                .flatMapValue(instructions -> wrapInstructions(state, instructions));
    }

    record Stack(JavaOrderedMap<String, Node> definitions) {
        public Stack() {
            this(new JavaOrderedMap<>());
        }

        private static Result<Long, CompileError> findLengthOfType(Node type) {
            return type.findInt(TYPE_LENGTH)
                    .<Result<Long, CompileError>>map(length -> new Ok<>((long) length))
                    .orElseGet(() -> new Err<>(new CompileError("No length present", new NodeContext(type))));
        }

        private Result<Long, CompileError> computeFrameSizeToSymbol(String symbol) {
            final var index = definitions.findIndexOfKey(symbol).orElse(0);
            final var slice = definitions.sliceToIndex(index).orElse(new JavaOrderedMap<>());
            return sumTypes(slice);
        }

        private Result<Long, CompileError> sumTypes(JavaOrderedMap<String, Node> definitions) {
            return definitions.stream()
                    .map(Tuple::right)
                    .map(Stack::findLengthOfType)
                    .into(ResultStream::new)
                    .foldResultsLeft(0L, Long::sum);
        }

        private Result<Long, CompileError> computeCurrentFrameSize() {
            return sumTypes(definitions);
        }

        public Stack define(String name, Node type) {
            return new Stack(definitions.put(name, type));
        }

        public Option<Node> find(String symbol) {
            return definitions.find(symbol);
        }
    }
}
