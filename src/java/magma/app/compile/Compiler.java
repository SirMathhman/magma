package magma.app.compile;

import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.CASMLang;
import magma.app.compile.lang.MagmaLang;

import java.util.Collections;
import java.util.List;

public record Compiler(String input) {
    static Passer createPasser() {
        return new CompoundPasser(List.of(
                new Passer() {
                    @Override
                    public Option<Result<Node, CompileError>> afterPass(Node node) {
                        return new Some<>(new Ok<>(new MapNode()
                                .withNodeList("children", Collections.emptyList())));
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