package magma.app.compile.rule;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.error.StringContext;

public class LazyRule implements Rule {
    private Option<Rule> childRule = new None<>();

    public void setRule(Rule rule) {
        this.childRule = new Some<>(rule);
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.map(rule -> rule.parse(input))
                .orElseGet(() -> new Err<>(new CompileError("No child rule set.", new StringContext(input))));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule.map(rule -> rule.generate(node))
                .orElseGet(() -> new Err<>(new CompileError("No child rule set", new NodeContext(node))));
    }
}