package magma.app.compile.pass;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.*;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
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

    private static Result<Tuple<JavaOrderedMap<String, Long>, JavaList<Node>>, CompileError> writeRootMember(JavaOrderedMap<String, Long> definitions, Node node) {
        return writeDeclaration(definitions, node)
                .or(() -> writeReturn(definitions, node))
                .orElseGet(() -> invalidateRootMember(node));
    }

    private static Option<Result<Tuple<JavaOrderedMap<String, Long>, JavaList<Node>>, CompileError>> writeReturn(JavaOrderedMap<String, Long> definitions, Node node) {
        if (!node.is(RETURN_TYPE)) return new None<>();

        final var returnValue = node.findNode(RETURN_VALUE).orElse(new MapNode());
        return new Some<>(loadValue(definitions, returnValue)
                .mapValue(list -> list.addAll(new JavaList<Node>()
                        .add(instruct("out"))
                        .add(instruct("halt"))))
                .mapValue(instructions -> new Tuple<>(definitions, instructions)));
    }

    private static Option<Result<Tuple<JavaOrderedMap<String, Long>, JavaList<Node>>, CompileError>> writeDeclaration(JavaOrderedMap<String, Long> definitions, Node node) {
        if (!node.is(DECLARATION_TYPE)) return new None<>();

        final var name = node.findString(DECLARATION_NAME).orElse("");
        final var value = node.findNode(DECLARATION_VALUE).orElse(new MapNode());

        return new Some<>(computeLength(value).flatMapValue(length -> pushValueToStack(definitions, value, 0L)
                .mapValue(list -> new Tuple<>(definitions.put(name, length), list))));
    }

    private static Result<Long, CompileError> computeLength(Node value) {
        if (value.is(NUMBER_TYPE)) return new Ok<>(1L);
        if (value.is(ARRAY_TYPE)) return value.findNodeList(ARRAY_VALUES).orElse(new JavaList<>())
                .stream()
                .map(RootPasser::computeLength)
                .into(ResultStream::new)
                .foldResultsLeft(1L, Long::sum);

        return new Err<>(new CompileError("Unknown value to compute length", new NodeContext(value)));
    }

    private static Result<JavaList<Node>, CompileError> pushValueToStack(JavaOrderedMap<String, Long> definitions, Node value, long initialOffset) {
        return loadValue(definitions, value).mapValue(instructions -> {
            final var totalOffset = definitions.stream()
                    .map(Tuple::right)
                    .foldLeft(initialOffset, Long::sum);

            return new JavaList<Node>()
                    .addAll(moveStackPointerRight(totalOffset))
                    .addAll(instructions)
                    .add(instructStackPointer("stoi"))
                    .addAll(moveStackPointerLeft(totalOffset));
        });
    }

    private static JavaList<Node> moveStackPointerLeft(long offset) {
        if (offset == 0) return new JavaList<>();
        return moveStackPointer(instruct("subv", offset));
    }

    private static JavaList<Node> moveStackPointerRight(long offset) {
        if (offset == 0) return new JavaList<>();
        return moveStackPointer(instruct("addv", offset));
    }

    private static JavaList<Node> moveStackPointer(Node instruction) {
        return new JavaList<Node>()
                .add(instruct("stod", DATA_CACHE))
                .add(instructStackPointer("ldd"))
                .add(instruction)
                .add(instructStackPointer("stod"))
                .add(instruct("ldd", DATA_CACHE));
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

    private static Result<JavaList<Node>, CompileError> loadValue(JavaOrderedMap<String, Long> definitions, Node node) {
        return loadNumber(node)
                .or(() -> loadSymbol(definitions, node))
                .or(() -> loadTuple(definitions, node))
                .or(() -> loadIndex(definitions, node))
                .orElseGet(() -> new Err<>(new CompileError("Unknown value", new NodeContext(node))));
    }

    private static Option<Result<JavaList<Node>, CompileError>> loadIndex(JavaOrderedMap<String, Long> definitions, Node node) {
        if (!node.is(INDEX_TYPE)) return new None<>();

        final var value = node.findNode(INDEX_VALUE).orElse(new MapNode());
        final var offset = node.findNode(INDEX_OFFSET).orElse(new MapNode());

        return new Some<>(loadValue(definitions, value).flatMapValue(loadedValue -> {
            return loadValue(definitions, offset).mapValue(loadedOffset -> {
                return loadedValue
                        .add(instruct("stod", DATA_CACHE))
                        .addAll(loadedOffset)
                        .add(instruct("addd", DATA_CACHE))
                        .add(instruct("stod", DATA_CACHE))
                        .add(instruct("ldi", DATA_CACHE));
            });
        }));
    }

    private static Option<Result<JavaList<Node>, CompileError>> loadTuple(JavaOrderedMap<String, Long> definitions, Node node) {
        if (!node.is(ARRAY_TYPE)) return new None<>();

        final var values = node.findNodeList(ARRAY_VALUES).orElse(new JavaList<>());
        final var list = values.streamWithIndex()
                .foldLeftToResult(new JavaList<Node>().addAll(moveStackPointerRight(1)), (current, tuple) -> {
                    final var index = tuple.left();
                    final var value = tuple.right();

                    return computeLength(value).flatMapValue(valueLength -> {
                        return loadValue(definitions, value).mapValue(instructions -> instructions
                                .add(instructStackPointer("stoi"))
                                .addAll(moveStackPointerRight(valueLength))).mapValue(current::addAll);
                    });
                })
                .flatMapValue(instructions -> {
                    return values.stream()
                            .map(RootPasser::computeLength)
                            .into(ResultStream::new)
                            .foldResultsLeft(0L, Long::sum).mapValue(sum -> {
                                return instructions.addAll(moveStackPointerLeft(sum))
                                        .add(instructStackPointer("ldd"))
                                        .addAll(moveStackPointerLeft(1));
                            });
                });

        return new Some<>(list);
    }

    private static Option<Result<JavaList<Node>, CompileError>> loadSymbol(JavaOrderedMap<String, Long> definitions, Node node) {
        if (!node.is(SYMBOL_TYPE)) return new None<>();

        final var value = node.findString(SYMBOL_VALUE).orElse("");
        final var index = definitions.findIndexOfKey(value).orElse(0);
        final var slice = definitions.sliceToIndex(index).orElse(new JavaOrderedMap<>());
        final var offset = slice.stream().map(Tuple::right).foldLeft(0L, Long::sum);

        return new Some<>(new Ok<>(new JavaList<Node>()
                .addAll(moveStackPointerRight(offset))
                .add(instructStackPointer("ldi"))
                .addAll(moveStackPointerLeft(offset))));
    }

    private static Node instructStackPointer(String mnemonic) {
        return instruct(mnemonic, STACK_POINTER);
    }

    private static Option<Result<JavaList<Node>, CompileError>> loadNumber(Node node) {
        if (!node.is(MagmaLang.NUMERIC_TYPE)) return new None<>();

        final var value = node.findInt(NUMERIC_VALUE).orElse(0);
        final var instructions = new JavaList<Node>()
                .add(instruct("ldv").withInt(INSTRUCTION_ADDRESS_OR_VALUE, value));

        return new Some<>(new Ok<>(instructions));
    }

    private static Result<Tuple<JavaOrderedMap<String, Long>, JavaList<Node>>, CompileError> invalidateRootMember(Node node) {
        final var context = new NodeContext(node);
        final var message = new CompileError("Cannot create instructions for root child", context);
        return new Err<>(message);
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
                .withString(BLOCK_BEFORE_CHILD, "\n\t")
                .withString(DATA_AFTER_NAME, " ")
                .withString(DATA_BEFORE_VALUE, " ")
                .withString(DATA_NAME, DATA_CACHE)
                .withNode(DATA_VALUE, cacheValue);

        final var dataValue = new MapNode(BLOCK_TYPE)
                .withNodeList(CHILDREN, List.of(cache))
                .withString(BLOCK_AFTER_CHILDREN, "\n");

        final var dataSection = new MapNode(SECTION_TYPE)
                .withString(GROUP_NAME, DATA_SECTION)
                .withString(GROUP_AFTER_NAME, " ")
                .withString(GROUP_AFTER, "\n")
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
                .foldLeftToResult(new Tuple<>(new JavaOrderedMap<>(), new JavaList<>()), RootPasser::foldRootMember)
                .mapValue(Tuple::right)
                .flatMapValue(instructions -> wrapInstructions(state, instructions));
    }
}
