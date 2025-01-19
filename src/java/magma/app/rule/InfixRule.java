package magma.app.rule;

import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
import magma.app.locate.Locator;

public final class InfixRule implements Rule {
    private final Rule leftRule;
    private final Locator locator;
    private final Rule rightRule;

    public InfixRule(Rule leftRule, Locator locator, Rule rightRule) {
        this.leftRule = leftRule;
        this.locator = locator;
        this.rightRule = rightRule;
    }

    public static Result<Tuple<String, String>, CompileError> split(Locator locator, String input) {
        return locator.locate(input).<Result<Tuple<String, String>, CompileError>>map(index -> {
            final var left = input.substring(0, index);
            final var right = input.substring(index + locator.length());
            final var tuple = new Tuple<String, String>(left, right);
            return new Ok<Tuple<String, String>, CompileError>(tuple);
        }).orElseGet(() -> new Err<Tuple<String, String>, CompileError>(new CompileError("Infix '" + locator.unwrap() + "' not present", new StringContext(input))));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return this.leftRule.generate(node).and(() -> this.rightRule.generate(node)).mapValue(tuple -> {
            return tuple.left() + this.locator.unwrap() + tuple.right();
        });
    }
}