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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static magma.Assembler.STACK_POINTER;
import static magma.app.compile.lang.CASMLang.*;
import static magma.app.compile.lang.MagmaLang.NUMBER_TYPE;
import static magma.app.compile.lang.MagmaLang.NUMBER_VALUE;
import static magma.app.compile.lang.MagmaLang.*;

class RootTransformer implements Passer {
    @Override
    public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
        if (!node.is(ROOT_TYPE)) return new None<>();

        final var list = new ArrayList<Node>();
        final var children = node.findNodeList(ROOT_CHILDREN).orElse(Collections.emptyList());

        var labels = new ArrayList<Tuple<String, Integer>>();
        for (Node child : children) {
            if (child.is(DECLARATION_TYPE)) {
                final var name = child.findString(DECLARATION_NAME).orElse("");
                labels.add(new Tuple<>(name, 1));

                list.addAll(List.of(
                        new MapNode("instruction").withString(MNEMONIC, "ldd").withString(INSTRUCTION_LABEL, STACK_POINTER),
                        new MapNode("instruction").withString(MNEMONIC, "addv").withInt(ADDRESS_OR_VALUE, 1),
                        new MapNode("instruction").withString(MNEMONIC, "stod").withString(INSTRUCTION_LABEL, STACK_POINTER)
                ));

                final var value = child.findNode(DECLARATION_VALUE).orElse(new MapNode());

                final var resolved = resolveValue(value, labels);
                if (resolved.isErr()) return resolved.findErr().map(Err::new);
                final var valueInstructions = resolved.findValue().orElse(Collections.emptyList());

                list.addAll(valueInstructions);
                list.add(new MapNode("instruction").withString(MNEMONIC, "stoi").withString(INSTRUCTION_LABEL, STACK_POINTER));
            } else if (child.is(RETURN_TYPE)) {
                final var returnValueOption = child.findNode(RETURN_VALUE);
                if (returnValueOption.isEmpty()) return new None<>();
                final var returnValue = returnValueOption.orElse(new MapNode());

                final var resolved = resolveValue(returnValue, labels);
                if (resolved.isErr()) return resolved.findErr().map(Err::new);
                final var valueInstructions = resolved.findValue().orElse(Collections.emptyList());
                list.addAll(valueInstructions);
                list.addAll(List.of(
                        new MapNode("instruction").withString(MNEMONIC, "out"),
                        new MapNode("instruction").withString(MNEMONIC, "halt")
                ));
            } else {
                return new Some<>(new Err<>(new CompileError("Unknown child", new NodeContext(child))));
            }
        }


        final var labelBlock = new MapNode("block").withNodeList("children", list);

        final var label = new MapNode("label")
                .withString("name", "__main__")
                .withNode("value", labelBlock);

        final var sectionBlock = new MapNode("block").withNodeList("children", List.of(label));
        final var section = new MapNode("section")
                .withString("name", "program")
                .withNode("value", sectionBlock);

        final var root = new MapNode().withNodeList("children", List.of(section));
        return new Some<>(new Ok<>(new Tuple<>(state, root)));
    }

    private Result<List<Node>, CompileError> resolveValue(Node value, List<Tuple<String, Integer>> labels) {
        if (value.is(NUMBER_TYPE)) {
            final var numericValueOption = value.findInt(NUMBER_VALUE);
            if (numericValueOption.isEmpty())
                return new Err<>(new CompileError("No numeric value present", new NodeContext(value)));

            final var numericValue = numericValueOption.orElse(0);
            return new Ok<>(List.of(
                    new MapNode("instruction").withString(MNEMONIC, "ldv").withInt(ADDRESS_OR_VALUE, numericValue)
            ));
        } else if (value.is(SYMBOL_TYPE)) {
            final var valueOption = value.findString(SYMBOL_VALUE);
            if (valueOption.isEmpty()) return new Err<>(new CompileError("No value in symbol", new NodeContext(value)));
            final var name = valueOption.orElse("");

            final var tuple = labels.stream()
                    .filter(label -> label.left().equals(name))
                    .findFirst();

            if (tuple.isEmpty()) {
                return new Err<>(new CompileError("Symbol not defined - " + name, new NodeContext(value)));
            }

            final var list = List.of(
                    new MapNode("instruction").withString(MNEMONIC, "ldi").withString(INSTRUCTION_LABEL, STACK_POINTER)
            );

            return new Ok<>(list);
        } else {
            return new Err<>(new CompileError("Unknown type", new NodeContext(value)));
        }
    }
}
