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
    private static Node instruct(String mnemonic) {
        return new MapNode(INSTRUCTION_TYPE)
                .withString(BLOCK_BEFORE_CHILD, "\n\t\t")
                .withString(MNEMONIC, mnemonic);
    }

    private static Result<Tuple<JavaOrderedMap<String, Long>, JavaList<Node>>, CompileError> parseRootMember(JavaOrderedMap<String, Long> state, Node node) {
        if (node.is(DECLARATION_TYPE)) {
            final var name = node.findString(DECLARATION_NAME).orElse("");
            final var value = node.findNode(DECLARATION_VALUE).orElse(new MapNode());

            final var added = state.put(name, 1L);
            return loadValue(value, state)
                    .mapValue(list -> list.add(instructStackPointer("stoi")))
                    .mapValue(list -> new Tuple<>(added, list));
        }

        if (node.is(RETURN_TYPE)) {
            final var value = node.findNode(RETURN_VALUE).orElse(new MapNode());
            return loadValue(value, state).mapValue(list -> list
                            .add(instruct("out"))
                            .add(instruct("halt")))
                    .mapValue(list -> new Tuple<>(state, list));
        }

        final var context = new NodeContext(node);
        final var message = new CompileError("Unknown root child", context);
        return new Err<>(message);
    }

    private static Result<JavaList<Node>, CompileError> loadValue(Node node, JavaOrderedMap<String, Long> state) {
        return parseNumberValue(node)
                .or(() -> parseSymbolValue(state, node))
                .orElseGet(() -> createUnknownValueError(node));
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
        final var index = state.findIndexOfKey(value);
        if (index.isEmpty()) {
            final var context = new StringContext(value);
            final var error = new CompileError("Symbol not defined", context);
            return new Some<>(new Err<>(error));
        }

        final var instructions = new JavaList<Node>().add(instructStackPointer("ldi"));
        return new Some<>(new Ok<>(instructions));
    }

    private static Node instruct(String mnemonic, String label) {
        return instruct(mnemonic).withString(INSTRUCTION_LABEL, label);
    }

    private static JavaList<Node> moveStackPointerLeft() {
        return moveStackPointer(instruct("subv", 1));
    }

    private static JavaList<Node> moveStackPointerRight() {
        return moveStackPointer(instruct("addv", 1));
    }

    private static JavaList<Node> moveStackPointer(Node adjustInstruction) {
        return new JavaList<Node>()
                .add(instructStackPointer("ldd"))
                .add(adjustInstruction)
                .add(instructStackPointer("stod"));
    }

    private static Node instructStackPointer(String mnemonic) {
        return instruct(mnemonic, STACK_POINTER);
    }

    private static Node instruct(String mnemonic, int addressOrValue) {
        return instruct(mnemonic).withInt(INSTRUCTION_ADDRESS_OR_VALUE, addressOrValue);
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

        final var sectionValue = new MapNode(BLOCK_TYPE)
                .withNodeList(CHILDREN, List.of(label))
                .withString(BLOCK_AFTER_CHILDREN, "\n");

        final var section = new MapNode(SECTION_TYPE)
                .withString(GROUP_NAME, SECTION_PROGRAM)
                .withString(GROUP_AFTER_NAME, " ")
                .withNode(GROUP_VALUE, sectionValue);

        return new Ok<>(new Tuple<>(state, new MapNode(ROOT_TYPE).withNodeList(CHILDREN, List.of(section))));
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
