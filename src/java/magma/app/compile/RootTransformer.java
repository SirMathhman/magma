package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.java.JavaList;

import java.util.Collections;
import java.util.List;

import static magma.Assembler.STACK_POINTER;
import static magma.app.compile.lang.CASMLang.*;
import static magma.app.compile.lang.MagmaLang.ROOT_CHILDREN;
import static magma.app.compile.lang.MagmaLang.ROOT_TYPE;

class RootTransformer implements Passer {
    private static void addStackPointer(List<Node> instructions, int amount) {
        moveStackPointer(instructions, amount, new MapNode("instruction").withString(MNEMONIC, "addv"));
    }

    private static void subtractStackPointer(List<Node> instructions, int amount) {
        moveStackPointer(instructions, amount, new MapNode("instruction").withString(MNEMONIC, "subv"));
    }

    private static void moveStackPointer(List<Node> instructions, int amount, Node instruction) {
        if (amount == 0) return;
        instructions.addAll(List.of(
                new MapNode("instruction").withString(MNEMONIC, "stod").withString(INSTRUCTION_LABEL, "temp"),
                new MapNode("instruction").withString(MNEMONIC, "ldd").withString(INSTRUCTION_LABEL, STACK_POINTER),
                instruction.withInt(ADDRESS_OR_VALUE, amount),
                new MapNode("instruction").withString(MNEMONIC, "stod").withString(INSTRUCTION_LABEL, STACK_POINTER),
                new MapNode("instruction").withString(MNEMONIC, "ldd").withString(INSTRUCTION_LABEL, "temp")
        ));
    }

    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        if (!node.is(ROOT_TYPE)) return new None<>();

        final var rootChildrenOption = node.findNodeList(ROOT_CHILDREN);
        if (rootChildrenOption.isEmpty())
            return new Some<>(new Err<>(new CompileError("No root children present", new NodeContext(node))));
        final var rootChildren = new JavaList<>(rootChildrenOption.orElse(Collections.emptyList()));

        var instructionsResult = rootChildren.stream().foldLeftToResult(new JavaList<>(), this::foldRootChild);
        if (instructionsResult.isErr()) return instructionsResult.findErr().map(Err::new);
        var instructions = instructionsResult.findValue().orElse(new JavaList<>());

        final var labelBlock = new MapNode("block").withNodeList("children", instructions.list());
        final var label = new MapNode("label")
                .withString("name", "__main__")
                .withNode("value", labelBlock);

        final var sectionBlock = new MapNode("block").withNodeList("children", List.of(label));
        final var dataSection = new MapNode("section")
                .withString("name", "data")
                .withNode("value", new MapNode("block").withNodeList("children", List.of(
                        new MapNode("data")
                                .withString("name", "temp")
                                .withNode("value", new MapNode("number")
                                        .withString("value", "0"))
                )));

        final var programSection = new MapNode("section")
                .withString("name", "program")
                .withNode("value", sectionBlock);

        final var root = new MapNode().withNodeList("children", List.of(dataSection, programSection));
        return new Some<>(new Ok<>(new Tuple<>(state, root)));
    }

    private Result<JavaList<Node>, CompileError> foldRootChild(JavaList<Node> current, Node node) {
        return new Err<>(new CompileError("Unknown node:", new NodeContext(node)));
    }
}
