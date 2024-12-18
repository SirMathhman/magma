package magma.app.compile.rule;

import magma.app.Input;
import magma.app.error.FormattedError;
import magma.app.error.InputContext;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Result;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    private Result<Node, FormattedError> parse0(String input) {
        if (input.startsWith(prefix)) {
            String input1 = input.substring(prefix.length());
            return childRule.parse(new Input(input1, 0, input1.length()));

        } else {
            return new Err<>(new CompileError("Prefix '" + prefix + "' not present", new InputContext(new Input(input, 0, input.length()))));
        }
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return childRule.generate(node).mapValue(generated -> prefix + generated);
    }

    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return parse0(input.slice());
    }
}
