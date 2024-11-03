package magma.app.compile;

import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.rule.*;

import java.util.List;

public record Compiler(String input) {
    public static final String RETURN_PREFIX = "return ";
    public static final String STATEMENT_END = ";";
    public static final String VALUE = "value";
    public static final String RETURN_TYPE = "return";

    private static Rule createReturnRule() {
        return new TypeRule(RETURN_TYPE, new PrefixRule(RETURN_PREFIX, new SuffixRule(new ExtractRule(VALUE), STATEMENT_END)));
    }

    private static Rule createCRootRule() {
        return new SplitRule(createReturnRule());
    }

    private static Rule createMagmaRootRule() {
        return new SplitRule(new StripRule(new OrRule(List.of(
                createDeclarationRule(),
                createReturnRule()
        ))));
    }

    private static TypeRule createDeclarationRule() {
        return new TypeRule("declaration", new FirstRule(new ExtractRule("before"), "=", new SuffixRule(new ExtractRule("value"), ";")));
    }

    public Result<String, CompileError> compile() {
        final var sourceRule = createMagmaRootRule();
        final var targetRule = createCRootRule();

        return sourceRule.parse(this.input())
                .flatMapValue(targetRule::generate)
                .mapValue(inner -> "int main(){\n\t" + inner + "\n}");
    }
}