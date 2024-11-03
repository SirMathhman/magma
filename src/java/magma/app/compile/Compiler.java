package magma.app.compile;

import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.rule.*;

public record Compiler(String input) {
    public static final String RETURN_PREFIX = "return ";
    public static final String STATEMENT_END = ";";
    public static final String VALUE = "value";

    private static Rule createReturnRule() {
        return new PrefixRule(RETURN_PREFIX, new SuffixRule(new ExtractRule(VALUE), STATEMENT_END));
    }

    private static Rule createCRootRule() {
        return new SplitRule(createReturnRule());
    }

    private static Rule createMagmaRootRule() {
        return new SplitRule(createReturnRule());
    }

    public Result<String, CompileError> compile() {
        final var sourceRule = createMagmaRootRule();
        final var targetRule = createCRootRule();

        return sourceRule.parse(this.input())
                .flatMapValue(targetRule::generate)
                .mapValue(inner -> "int main(){\n\t" + inner + "\n}");
    }
}