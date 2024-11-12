package magma.app.compile;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.CASMLang;
import magma.app.compile.lang.MagmaLang;

import java.util.List;

import static magma.app.compile.lang.CASMLang.*;

public record Compiler(String input) {
    static Passer createPasser() {
        return new CompoundPasser(List.of(
                new Passer() {
                    @Override
                    public Option<Result<Node, CompileError>> afterPass(Node node) {
                        return new Some<>(new Ok<>(new MapNode()
                                .withNodeList("children", List.of(
                                        new MapNode("section")
                                                .withString("name", "program")
                                                .withNodeList("children", List.of(
                                                        new MapNode("label")
                                                                .withString("name", "__main__")
                                                                .withNodeList("children", List.of(
                                                                        new MapNode("instruction")
                                                                                .withString(MNEMONIC, "ldv")
                                                                                .withInt(ADDRESS_OR_VALUE, 0),
                                                                        new MapNode("instruction")
                                                                                .withString(MNEMONIC, "out"),
                                                                        new MapNode("instruction")
                                                                                .withString(MNEMONIC, "halt")
                                                                ))
                                                ))
                                ))));
                    }
                }
        ));
    }

    private static CompoundPassingStage createPassingStage() {
        return new CompoundPassingStage(List.of(
                new TreePassingStage(createPasser()),
                new TreePassingStage(createFormattingPasser())
        ));
    }

    private static Passer createFormattingPasser() {
        return new Passer() {
            @Override
            public Option<Result<Node, CompileError>> afterPass(Node node) {
                if(!node.is(SECTION_TYPE)) return new None<>();

                return new Some<>(new Ok<>(node.withString(GROUP_AFTER_NAME, " ")));
            }
        };
    }

    public Result<String, CompileError> compile() {
        final var sourceRule = MagmaLang.createMagmaRootRule();
        final var targetRule = CASMLang.createRootRule();

        return sourceRule.parse(this.input())
                .flatMapValue(createPassingStage()::pass)
                .flatMapValue(targetRule::generate);
    }
}