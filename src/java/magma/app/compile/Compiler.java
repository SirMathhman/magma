package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.format.BlockFormatter;
import magma.app.compile.format.SectionFormatter;
import magma.app.compile.lang.CASMLang;
import magma.app.compile.lang.MagmaLang;

import java.util.Collections;
import java.util.List;

import static magma.app.compile.lang.CASMLang.ADDRESS_OR_VALUE;
import static magma.app.compile.lang.CASMLang.MNEMONIC;
import static magma.app.compile.lang.MagmaLang.*;

public record Compiler(String input) {
    static Passer createPasser() {
        return new CompoundPasser(List.of(
                new Transformer()
        ));
    }

    private static CompoundPassingStage createPassingStage() {
        return new CompoundPassingStage(List.of(
                new TreePassingStage(createPasser()),
                new TreePassingStage(createFormattingPasser())
        ));
    }

    private static Passer createFormattingPasser() {
        return new CompoundPasser(List.of(
                new SectionFormatter(),
                new BlockFormatter()
        ));
    }

    public Result<String, CompileError> compile() {
        final var sourceRule = MagmaLang.createMagmaRootRule();
        final var targetRule = CASMLang.createRootRule();

        return sourceRule.parse(this.input())
                .flatMapValue(node -> createPassingStage().pass(new State(), node))
                .mapValue(Tuple::right)
                .flatMapValue(targetRule::generate);
    }

    private static class Transformer implements Passer {
        @Override
        public Option<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node) {
            if (!node.is(ROOT_TYPE)) return new None<>();

            final var children = node.findNodeList(ROOT_CHILDREN).orElse(Collections.emptyList());

            var value = 0;
            for (Node child : children) {
                if (child.is(RETURN_TYPE)) {
                    final var returnValueOption = child.findNode(RETURN_VALUE);
                    if (returnValueOption.isEmpty()) return new None<>();
                    final var returnValue = returnValueOption.orElse(new MapNode());

                    final var numberValue = returnValue.findInt(NUMBER_VALUE);
                    if (numberValue.isEmpty()) return new None<>();
                    value = numberValue.orElse(0);
                }
            }

            final var labelBlock = new MapNode("block")
                    .withNodeList("children", List.of(
                            new MapNode("instruction")
                                    .withString(MNEMONIC, "ldv")
                                    .withInt(ADDRESS_OR_VALUE, value),
                            new MapNode("instruction")
                                    .withString(MNEMONIC, "out"),
                            new MapNode("instruction")
                                    .withString(MNEMONIC, "halt")
                    ));

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
    }
}