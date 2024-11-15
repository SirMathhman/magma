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
import magma.app.compile.pass.Passer;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.lang.CASMLang;
import magma.java.JavaList;

import static magma.Assembler.MAIN;
import static magma.Assembler.SECTION_PROGRAM;
import static magma.app.compile.lang.CASMLang.*;
import static magma.app.compile.lang.MagmaLang.ROOT_TYPE;

public class InstructionWrapper implements Passer {
    public static final String DATA_SECTION = "data";
    public static final String DATA_CACHE = "cache";

    private static Tuple<State, Node> wrapInstructions(State state, JavaList<Node> instructions) {
        final var labelValue = new MapNode(BLOCK_TYPE)
                .withNodeList(CHILDREN, instructions)
                .withString(BLOCK_AFTER_CHILDREN, "\n\t");

        final var label = new MapNode(LABEL_TYPE)
                .withString(GROUP_NAME, MAIN)
                .withString(GROUP_AFTER_NAME, " ")
                .withString(BLOCK_BEFORE_CHILD, "\n\t")
                .withNode(GROUP_VALUE, labelValue);

        final var programValue = new MapNode(BLOCK_TYPE)
                .withNodeList(CHILDREN, new JavaList<Node>().addLast(label))
                .withString(BLOCK_AFTER_CHILDREN, "\n");

        final var programSection = new MapNode(SECTION_TYPE)
                .withString(GROUP_AFTER_NAME, " ")
                .withString(GROUP_NAME, SECTION_PROGRAM)
                .withNode(GROUP_VALUE, programValue);

        final var cacheValue = new MapNode(CASMLang.NUMBER_TYPE)
                .withString(CASMLang.NUMBER_VALUE, "0");

        final var cache = new MapNode(DATA_TYPE)
                .withString(BLOCK_BEFORE_CHILD, "\n\t")
                .withString(DATA_NAME, DATA_CACHE)
                .withNode(DATA_VALUE, cacheValue);

        final var dataValue = new MapNode(BLOCK_TYPE)
                .withString(BLOCK_AFTER_CHILDREN, "\n")
                .withNodeList(CHILDREN, new JavaList<Node>().addLast(cache));

        final var dataSection = new MapNode(SECTION_TYPE)
                .withString(GROUP_AFTER_NAME, " ")
                .withString(GROUP_AFTER, "\n")
                .withString(GROUP_NAME, DATA_SECTION)
                .withNode(GROUP_VALUE, dataValue);

        final var sectionList = new JavaList<Node>()
                .addLast(dataSection)
                .addLast(programSection);

        final var node = new MapNode(ROOT_TYPE).withNodeList(CHILDREN, sectionList);
        return new Tuple<>(state, node);
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

        return new Ok<>(wrapInstructions(state, new JavaList<>()));
    }
}
