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

    private static Result<Tuple<JavaOrderedMap<String, Long>, JavaList<Node>>, CompileError> writeRootMember(JavaOrderedMap<String, Long> definitions, Node node) {
        return writeDeclaration(definitions, node).orElseGet(() -> invalidateRootMember(node));
    }

    private static Option<Result<Tuple<JavaOrderedMap<String, Long>, JavaList<Node>>, CompileError>> writeDeclaration(JavaOrderedMap<String, Long> definitions, Node node) {
        if (!node.is(DECLARATION_TYPE)) return new None<>();

        final var name = node.findString(DECLARATION_NAME).orElse("");
        final var value = node.findNode(DECLARATION_VALUE).orElse(new MapNode());

        return new Some<>(loadValue(value).mapValue(valueInstructions -> {
            final var list = valueInstructions.add(createInstruction("stoi")
                    .withString(INSTRUCTION_LABEL, STACK_POINTER));
            return new Tuple<>(definitions, list);
        }));
    }

    private static Node createInstruction(String mnemonic) {
        return new MapNode(INSTRUCTION_TYPE)
                .withString(BLOCK_BEFORE_CHILD, "\n\t\t")
                .withString(INSTRUCTION_MNEMONIC, mnemonic);
    }

    private static Result<JavaList<Node>, CompileError> loadValue(Node node) {
        return loadNumber(node).orElseGet(() -> new Err<>(new CompileError("Unknown value", new NodeContext(node))));
    }

    private static Option<Result<JavaList<Node>, CompileError>> loadNumber(Node node) {
        if (!node.is(MagmaLang.NUMERIC_TYPE)) return new None<>();

        final var value = node.findInt(NUMERIC_VALUE).orElse(0);
        final var instructions = new JavaList<Node>()
                .add(createInstruction("ldv").withInt(INSTRUCTION_ADDRESS_OR_VALUE, value));

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
                .withString(DATA_NAME, "cache")
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
