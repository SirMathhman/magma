package magma.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.Context;
import magma.compile.error.NodeContext;
import magma.compile.error.StringContext;

import java.util.Optional;

public class LazyRule implements Rule {
    private Optional<Rule> maybeRule = Optional.empty();

    public void set(Rule rule) {
        this.maybeRule = Optional.of(rule);
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return maybeRule.map(rule -> rule.parse(input)).orElseGet(() -> createErr(new StringContext(input)));
    }

    private <T> Result<T, CompileError> createErr(Context context) {
        return new Err<>(new CompileError("Rule not set", context));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return maybeRule.map(rule -> rule.generate(node)).orElseGet(() -> createErr(new NodeContext(node)));
    }
}
