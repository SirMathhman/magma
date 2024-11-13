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
import magma.java.JavaList;

import java.util.List;

import static magma.Assembler.*;
import static magma.app.compile.lang.CASMLang.*;
import static magma.app.compile.lang.MagmaLang.ROOT_TYPE;
import static magma.app.compile.lang.MagmaLang.*;

public class RootPasser implements Passer {
    private static Node instruct(String mnemonic) {
        return new MapNode(INSTRUCTION_TYPE)
                .withString(BLOCK_BEFORE_CHILD, "\n\t\t")
                .withString(MNEMONIC, mnemonic);
    }

    private static Result<JavaList<Node>, CompileError> parseRootMember(Node node) {
        if (node.is(DECLARATION_TYPE)) {
            final var valueOption = node.findString(DECLARATION_VALUE);
            if(valueOption.isEmpty()) return new Err<>(new CompileError("No value present", new NodeContext(node)));
            final var value = Integer.parseInt(valueOption.orElse(""), 10);

            return new Ok<>(new JavaList<Node>()
                    .add(instruct("ldv", value))
                    .add(instructStackPointer("stoi")));
        }

        if (node.is(RETURN_TYPE)) {
            final var value = node.findString(RETURN_VALUE)
                    .map(Integer::parseUnsignedInt)
                    .orElse(0);

            return new Ok<>(new JavaList<Node>()
                    .add(instruct("ldv", value))
                    .add(instruct("out"))
                    .add(instruct("halt")));
        }

        final var context = new NodeContext(node);
        final var message = new CompileError("Unknown root child", context);
        return new Err<>(message);
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

    private static Ok<Tuple<State, Node>, CompileError> getTupleCompileErrorOk(State state, JavaList<Node> instructions) {
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

        final var children = childrenOption.orElse(new JavaList<Node>());
        return children.stream()
                .map(RootPasser::parseRootMember)
                .into(ResultStream::new)
                .foldResultsLeft(new JavaList<Node>(), JavaList::addAll)
                .flatMapValue(instructions -> getTupleCompileErrorOk(state, instructions));
    }
}
