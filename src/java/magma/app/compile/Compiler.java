package magma.app.compile;

import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.CLang;
import magma.app.compile.lang.MagmaLang;

public record Compiler(String input) {
    public Result<String, CompileError> compile() {
        final var sourceRule = MagmaLang.createMagmaStatementsRule();
        final var targetRule = CLang.createCRootRule();

        return sourceRule.parse(this.input())
                .flatMapValue(Passer::pass)
                .flatMapValue(targetRule::generate);
    }
}