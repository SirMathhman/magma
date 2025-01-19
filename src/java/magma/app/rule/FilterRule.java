package magma.app.rule;

import magma.app.Node;
import magma.app.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.error.context.StringContext;

import java.util.function.Function;
import java.util.function.Predicate;

public record FilterRule(Predicate<String> filter,
                         Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> apply(String input) {
        if (this.filter.test(input)) {
            return this.childRule.apply(input);
        } else {
            return new Err<>(new CompileError("Filter did not apply", new StringContext(input)));
        }
    }
}
