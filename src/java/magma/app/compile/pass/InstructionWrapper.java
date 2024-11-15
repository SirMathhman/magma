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
import magma.app.compile.lang.CASMLang;
import magma.java.JavaList;

import java.util.List;

import static magma.Assembler.MAIN;
import static magma.Assembler.SECTION_PROGRAM;
import static magma.app.compile.lang.CASMLang.*;
import static magma.app.compile.lang.MagmaLang.ROOT_TYPE;

public class InstructionWrapper implements Passer {
    public static final String DATA_SECTION = "data";
    public static final String DATA_CACHE = "cache";

    private static Result<Tuple<State, Node>, CompileError> wrapInstructions(State state, JavaList<Node> instructions) {
        var node4 = new MapNode(BLOCK_TYPE);
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
                .withString(GROUP_NAME, SECTION_PROGRAM)
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

        var node1 = new MapNode(ROOT_TYPE);
        final var node = node1.withNodeList(CHILDREN, new JavaList<>(List.of(dataSection, programSection)));

        return new Ok<>(new Tuple<>(state, node));
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

        return wrapInstructions(state, new JavaList<>());
    }
}
