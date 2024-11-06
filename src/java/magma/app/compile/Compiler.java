package magma.app.compile;

import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.CLang;
import magma.app.compile.lang.MagmaLang;

import java.util.Collections;

public record Compiler(String input) {
    static Passer createPasser() {
        return new CompoundPasser(Collections.emptyList());
    }

    public Result<String, CompileError> compile() {
        final var sourceRule = MagmaLang.createMagmaRootRule();
        final var targetRule = CLang.createCRootRule();

        return sourceRule.parse(this.input())
                .flatMapValue(new TreePassingStage(createPasser())::pass)
                .flatMapValue(targetRule::generate);
    }

}