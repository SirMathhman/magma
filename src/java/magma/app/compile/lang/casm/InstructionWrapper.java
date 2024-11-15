package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.ResultStream;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.error.StringContext;
import magma.app.compile.lang.CASMLang;
import magma.app.compile.pass.Passer;
import magma.java.JavaList;

import static magma.Assembler.*;
import static magma.app.compile.lang.CASMLang.ROOT_TYPE;
import static magma.app.compile.lang.CASMLang.*;
import static magma.app.compile.lang.MagmaLang.*;
import static magma.app.compile.lang.casm.Instructions.instruct;

public class InstructionWrapper implements Passer {
    public static final String DATA_SECTION = "data";
    public static final String DATA_CACHE = "cache";

    private static Node wrapInstructions(JavaList<Node> instructions) {
        final var labelValue = new MapNode(BLOCK_TYPE).withNodeList(CHILDREN, instructions);
        final var label = new MapNode(LABEL_TYPE)
                .withString(GROUP_NAME, MAIN)
                .withNode(GROUP_VALUE, labelValue);

        final var programValue = new MapNode(BLOCK_TYPE).withNodeList(CHILDREN, new JavaList<Node>().addLast(label));
        final var programSection = new MapNode(SECTION_TYPE)
                .withString(GROUP_NAME, SECTION_PROGRAM)
                .withNode(GROUP_VALUE, programValue);

        final var cacheValue = new MapNode(CASMLang.NUMBER_TYPE).withString(CASMLang.NUMBER_VALUE, "0");
        final var cache = new MapNode(DATA_TYPE)
                .withString(DATA_NAME, DATA_CACHE)
                .withNode(DATA_VALUE, cacheValue);

        final var dataValue = new MapNode(BLOCK_TYPE).withNodeList(CHILDREN, new JavaList<Node>().addLast(cache));
        final var dataSection = new MapNode(SECTION_TYPE)
                .withString(GROUP_NAME, DATA_SECTION)
                .withNode(GROUP_VALUE, dataValue);

        final var sectionList = new JavaList<Node>().addLast(dataSection).addLast(programSection);
        return new MapNode(ROOT_TYPE).withNodeList(CHILDREN, sectionList);
    }

    private static Option<Result<Tuple<State, JavaList<Node>>, CompileError>> foldReturnStatement(State state, Node node) {
        if (!node.is(RETURN_TYPE)) return new None<>();

        final var value = node.findNode(RETURN_VALUE).orElse(new MapNode());
        return new Some<>(loadValue(state, value).mapValue(tuple -> {
            return new Tuple<>(state, new JavaList<Node>()
                    .addAll(tuple.findFirst().orElse(new JavaList<>()))
                    .addLast(instruct("out"))
                    .addLast(instruct("halt")));
        }));
    }

    private static Result<JavaList<JavaList<Node>>, CompileError> loadValue(State state, Node node) {
        return loadNumericValue(node)
                .or(() -> loadSymbolValue(state, node))
                .or(() -> loadTupleType(state, node))
                .orElseGet(() -> new Err<>(new CompileError("Value not loadable", new NodeContext(node))));
    }

    private static Option<Result<JavaList<JavaList<Node>>, CompileError>> loadTupleType(State state, Node node) {
        if (!node.is(TUPLE_TYPE)) return new None<>();

        final var values = node.findNodeList(TUPLE_VALUES).orElse(new JavaList<>());
        return new Some<>(values.stream()
                .map(value -> loadValue(state, value))
                .into(ResultStream::new)
                .foldResultsLeft(new JavaList<JavaList<Node>>(), JavaList::addAll)
                .mapValue(list -> {
                    final var element = new JavaList<Node>()
                            .addLast(instruct("ldd", STACK_POINTER))
                            .addLast(instruct("addv", 1));
                    return list.addFirst(element);
                }));
    }

    private static Option<Result<JavaList<JavaList<Node>>, CompileError>> loadSymbolValue(State state, Node node) {
        if (!node.is(SYMBOL_TYPE)) return new None<>();

        final var value = node.findString(SYMBOL_VALUE).orElse("");
        final var addressOption = state.lookup(value);
        if (addressOption.isEmpty())
            return new Some<>(new Err<>(new CompileError("Symbol not defined", new StringContext(value))));

        final var address = addressOption.orElse(0L);
        final var instructions = state.stack().moveToAddress(address).right()
                .addLast(instruct("ldi", STACK_POINTER));

        return new Some<>(new Ok<>(new JavaList<JavaList<Node>>().addLast(instructions)));
    }

    private static Option<Result<JavaList<JavaList<Node>>, CompileError>> loadNumericValue(Node node) {
        if (!node.is(NUMERIC_TYPE)) return new None<>();

        final var value = node.findInt(NUMERIC_VALUE).orElse(0);
        final var instructions = new JavaList<Node>()
                .addLast(instruct("ldv", value));

        return new Some<>(new Ok<>(new JavaList<JavaList<Node>>()
                .addLast(instructions)));
    }

    private static Err<Tuple<State, JavaList<Node>>, CompileError> invalidateRootMember(Node node) {
        final var context = new NodeContext(node);
        final var error = new CompileError("Unknown root member", context);
        return new Err<>(error);
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
                .foldLeftToResult(new Tuple<>(state, new JavaList<>()), this::foldRootMember)
                .mapValue(tuple -> tuple.mapRight(InstructionWrapper::wrapInstructions));
    }

    private Result<Tuple<State, JavaList<Node>>, CompileError> foldRootMember(Tuple<State, JavaList<Node>> current, Node rootMember) {
        final var state = current.left();
        final var instructions = current.right();
        return foldDeclaration(state, rootMember)
                .or(() -> foldReturnStatement(state, rootMember))
                .orElseGet(() -> invalidateRootMember(rootMember))
                .mapValue(tuple -> tuple.mapRight(instructions::addAll));
    }

    private Option<Result<Tuple<State, JavaList<Node>>, CompileError>> foldDeclaration(State state, Node declaration) {
        if (!declaration.is(DECLARATION_TYPE)) return new None<>();

        final var name = declaration.findString(DECLARATION_NAME).orElse("");
        final var value = declaration.findNode(DECLARATION_VALUE).orElse(new MapNode());

        final var clearStack = state.stack().moveToEmptyAddress();
        return new Some<>(loadValue(new State(state.frames(), clearStack.left()), value).mapValue(instructions -> {
            final var pushed = state.stack().pushMultipleData(instructions);
            final var newStack = pushed.left();
            final var address = pushed.right().left();
            final var instructions0 = clearStack.right().addAll(pushed.right().right());
            final var defined = state.define(name, address);
            return new Tuple<>(new State(defined.frames(), newStack), instructions0);
        }));
    }
}
