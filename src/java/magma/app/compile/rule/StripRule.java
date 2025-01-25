package magma.app.compile.rule;

import magma.api.result.Result;
import magma.app.compile.node.Input;
import magma.app.compile.node.Node;
import magma.app.error.CompileError;
import magma.java.JavaOptions;

public record StripRule(
        Rule childRule, String before, String after
) implements Rule {
    public StripRule(Rule childRule) {
        this(childRule, "", "");
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return this.childRule.parse(input.strip());
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        final var before = JavaOptions.toNative(node.inputs().find(this.before).map(Input::unwrap)).orElse("");
        final var after = JavaOptions.toNative(node.inputs().find(this.after).map(Input::unwrap)).orElse("");
        return this.childRule.generate(node).mapValue(content -> before + content + after);
    }
}
