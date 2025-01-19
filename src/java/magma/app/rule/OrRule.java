package magma.app.rule;

import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.api.stream.Stream;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public record OrRule(Stream<Rule> rules) implements Rule {
    public static Result<Node, CompileError> or(String type, String input, Stream<Supplier<Result<Node, CompileError>>> stream) {
        return stream.map(OrRule::prepare)
                .foldLeft(Supplier::get, (current, next) -> current.or(next).mapErr(OrRule::merge))
                .map(result -> result.mapErr(errors -> new CompileError("Invalid " + type, new StringContext(input), errors)))
                .orElseGet(() -> new Err<>(new CompileError("No compilers present", new StringContext(input))));
    }

    public static List<CompileError> merge(Tuple<List<CompileError>, List<CompileError>> tuple) {
        final var left = tuple.left();
        final var right = tuple.right();
        final var copy = new ArrayList<>(left);
        copy.addAll(right);
        return copy;
    }

    public static Supplier<Result<Node, List<CompileError>>> prepare(
            Supplier<Result<Node, CompileError>> supplier
    ) {
        return () -> supplier.get().mapErr(Collections::singletonList);
    }

    @Override
    public Result<Node, CompileError> parse(String value) {
        return or("value", value, this.rules.map(rule -> () -> rule.parse(value)));
    }
}
