package magma.compile;

import magma.compile.rule.ExtractRule;
import magma.compile.rule.PrefixRule;
import magma.compile.rule.Rule;
import magma.compile.rule.SuffixRule;

public record Compiler(String input) {
    public static final String RETURN_PREFIX = "return ";
    public static final String STATEMENT_END = ";";
    public static final String VALUE = "value";

    private static Rule createReturnRule() {
        return new PrefixRule(RETURN_PREFIX, new SuffixRule(new ExtractRule(VALUE), STATEMENT_END));
    }

    public String compile() {
        final var result = createMagmaRootRule().parse(input())
                .flatMap(node -> createCRootRule().generate(node))
                .orElse("");

        return "int main(){\n\t" + result + "\n}";
    }

    private static Rule createCRootRule() {
        return createReturnRule();
    }

    private static Rule createMagmaRootRule() {
        return createReturnRule();
    }
}