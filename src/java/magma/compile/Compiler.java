package magma.compile;

import magma.compile.lang.CommonLang;
import magma.compile.rule.Rule;
import magma.core.String_;
import magma.core.result.Result;

public record Compiler(String_ input) {
    private static Rule createRootRule() {
        return CommonLang.createImportRule();
    }

    public Result<String_, CompileError> compile() {
        return compileImport();
    }

    private Result<String_, CompileError> compileImport() {
        return createRootRule().parse(input).flatMapValue(mapNode -> {
            Rule rule1 = createRootRule();
            return rule1.generate(mapNode);
        });
    }
}