package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.lang.CASMLang;
import magma.app.compile.pass.Passer;
import magma.java.JavaList;

import static magma.Assembler.MAIN;
import static magma.Assembler.SECTION_PROGRAM;
import static magma.app.compile.lang.CASMLang.*;
import static magma.app.compile.lang.CASMLang.ROOT_TYPE;
import static magma.app.compile.lang.MagmaLang.*;

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
        return new Some<>(loadValue(value).mapValue(loadInstructions -> {
            final var instruction = instruct("halt");

            return new Tuple<>(state, new JavaList<Node>()
                    .addAll(loadInstructions)
                    .addLast(instruction));
        }));
    }

    private static Result<JavaList<Node>, CompileError> loadValue(Node node) {
        if(node.is(NUMERIC_TYPE)) {
            final var value = node.findInt(NUMERIC_VALUE).orElse(0);
            return new Ok<>(new JavaList<Node>()
                    .addLast(instruct("ldv", value))
                    .addLast(instruct("out")));
        }

        return new Err<>(new CompileError("Unknown value", new NodeContext(node)));
    }

    private static Node instruct(String mnemonic, long value) {
        return instruct(mnemonic)
                .withInt(INSTRUCTION_ADDRESS_OR_VALUE, (int) value);
    }

    private static Node instruct(String mnemonic) {
        return new MapNode(INSTRUCTION_TYPE)
                .withString(INSTRUCTION_MNEMONIC, mnemonic);
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
        return foldReturnStatement(current.left(), rootMember)
                .orElseGet(() -> invalidateRootMember(rootMember))
                .mapValue(tuple -> tuple.mapRight(current.right()::addAll));
    }
}
