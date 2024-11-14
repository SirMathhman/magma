package magma.app.compile.rule;

import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.StringContext;

import java.util.Objects;

public final class FirstRule implements Rule {
    private final Rule leftRule;
    private final String slice;
    private final Rule rightRule;
    private final Locator locator;

    public FirstRule(Locator locator, Rule leftRule, String slice, Rule rightRule) {
        this.leftRule = leftRule;
        this.slice = slice;
        this.rightRule = rightRule;
        this.locator = locator;
    }

    private static Result<Node, CompileError> merge(String input, Tuple<Node, Node> tuple) {
        return tuple.left().merge(tuple.right())
                .<Result<Node, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("Types present for both nodes.", new StringContext(input))));
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return locator.locate(input).map(index -> {
            final var left = input.substring(0, index);
            final var right = input.substring(index + slice.length());

            return leftRule.parse(left).and(() -> rightRule.parse(right)).flatMapValue(tuple -> merge(input, tuple));
        }).orElseGet(() -> {
            final var format = "Slice '%s' not present.";
            final var message = format.formatted(slice);
            return new Err<>(new CompileError(message, new StringContext(input)));
        });
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return leftRule.generate(node)
                .and(() -> rightRule.generate(node))
                .mapValue(tuple -> tuple.left() + slice + tuple.right());
    }

    public Rule leftRule() {
        return leftRule;
    }

    public String slice() {
        return slice;
    }

    public Rule rightRule() {
        return rightRule;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FirstRule) obj;
        return Objects.equals(this.leftRule, that.leftRule) &&
                Objects.equals(this.slice, that.slice) &&
                Objects.equals(this.rightRule, that.rightRule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftRule, slice, rightRule);
    }

    @Override
    public String toString() {
        return "FirstRule[" +
                "leftRule=" + leftRule + ", " +
                "slice=" + slice + ", " +
                "rightRule=" + rightRule + ']';
    }

}
