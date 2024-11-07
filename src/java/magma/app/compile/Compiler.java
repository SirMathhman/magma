package magma.app.compile;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.CLang;
import magma.app.compile.lang.MagmaLang;

import java.util.List;

public record Compiler(String input) {
    static Passer createPasser() {
        return new CompoundPasser(List.of(
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
        final var targetRule = CLang.createCRootRule();

        return sourceRule.parse(this.input())
                .flatMapValue(createPassingStage()::pass)
                .flatMapValue(targetRule::generate);
    }
}