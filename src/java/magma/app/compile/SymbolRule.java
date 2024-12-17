package magma.app.compile;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.rule.Rule;
import magma.app.error.CompileError;
import magma.app.error.FormattedError;
import magma.app.error.StringContext;

public record SymbolRule(Rule childRule) implements Rule {
    private static boolean isSymbol(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isLetter(input.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Result<Node, FormattedError> parse(String input) {
        if (isSymbol(input)) {
            return childRule.parse(input);
        } else {
            return new Err<>(new CompileError("Not a symbol", new StringContext(input)));
        }
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return childRule.generate(node);
    }
}
