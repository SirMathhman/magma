package magma.compile;

import magma.compile.lang.CommonLang;
import magma.compile.rule.EmptyRule;
import magma.compile.rule.OrRule;
import magma.compile.rule.Rule;
import magma.core.String_;
import magma.core.result.Result;
import magma.java.JavaList;

public record Compiler(String_ input) {
    private static Rule createRootRule() {
        return new OrRule(new JavaList<Rule>()
                .add(CommonLang.createImportRule())
                .add(new EmptyRule())
        );
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