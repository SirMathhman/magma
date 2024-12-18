package magma.app.compile.rule;

import magma.api.java.MutableJavaList;
import magma.app.error.ContextDetail;
import magma.app.error.FormattedError;
import magma.app.error.InputContext;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Result;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return input.input().startsWith(prefix)
                ? childRule.parse(new Input(input.input().substring(prefix.length())))
                : new Err<>(new CompileError(new ContextDetail("Prefix '" + prefix + "' not present", new InputContext(new Input(input.input()))), new MutableJavaList<>()));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return childRule.generate(node).mapValue(generated -> prefix + generated);
    }
}
