package magma.app.compile.rule;

import magma.app.compile.Node;
import magma.app.compile.CompileError;
import magma.app.error.Context;
import magma.app.error.NodeContext;
import magma.app.error.StringContext;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Result;

public class LazyRule implements Rule {
    private Option<Rule> option = new None<>();

    private static <T> Err<T, CompileError> createErr(Context context) {
        final var error = new CompileError("Rule not set", context);
        return new Err<>(error);
    }

    public void set(Rule orRule) {
        option = new Some<>(orRule);
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return option
                .map(inner -> inner.parse(input))
                .orElseGet(() -> createErr(new StringContext(input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return option
                .map(inner -> inner.generate(node))
                .orElseGet(() -> createErr(new NodeContext(node)));
    }
}
