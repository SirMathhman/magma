package magma.rule;

import magma.Node;
import magma.error.CompileError;
import magma.result.Err;
import magma.result.Result;

import java.util.function.Function;
import java.util.function.Predicate;

public record FilterRule(Predicate<String> filter,
                         Function<String, Result<Node, CompileError>> childRule) implements Function<String, Result<Node, CompileError>> {
    @Override
    public Result<Node, CompileError> apply(String input) {
        if (this.filter.test(input)) {
            return this.childRule.apply(input);
        } else {
            return new Err<>(new CompileError("Filter did not apply", input));
        }
    }
}
