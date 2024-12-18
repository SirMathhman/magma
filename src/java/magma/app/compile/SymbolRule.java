package magma.app.compile;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.Input;
import magma.app.compile.rule.Rule;
import magma.app.error.CompileError;
import magma.app.error.FormattedError;
import magma.app.error.InputContext;

public record SymbolRule(Rule childRule) implements Rule {
    private static boolean isSymbol(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isLetter(input.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private Result<Node, FormattedError> parse0(String input) {
        if (isSymbol(input)) {
            return childRule.parse(new Input(input, 0, input.length()));

        } else {
            return new Err<>(new CompileError("Not a symbol", new InputContext(new Input(input, 0, input.length()))));
        }
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return childRule.generate(node);
    }

    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return parse0(input.slice());
    }
}
