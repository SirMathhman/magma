package magma.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.StringContext;

public record SymbolRule(Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (isSymbol(input)) return childRule.parse(input);
        else return new Err<>(new CompileError("Not a symbol", new StringContext(input)));
    }

    private boolean isSymbol(String input) {
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            if (!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule.generate(node);
    }
}
