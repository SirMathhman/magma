package magma.app.compile;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.rule.Input;
import magma.app.compile.rule.Rule;
import magma.app.error.CompileError;
import magma.app.error.FormattedError;
import magma.app.error.InputContext;

public record SymbolRule(Rule childRule) implements Rule {
    @Override
    public Result<Node, FormattedError> parse(Input input) {
        if (isSymbol(input)) {
            return childRule.parse(new Input(input.input()));
        } else {
            return new Err<>(new CompileError("Not a symbol", new InputContext(new Input(input.input()))));
        }
    }

    private static boolean isSymbol(Input input) {
        for (int i = 0; i < input.input().length(); i++) {
            if (!Character.isLetter(input.input().charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return childRule.generate(node);
    }
}
