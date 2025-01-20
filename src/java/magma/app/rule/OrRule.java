package magma.app.rule;

import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Stream;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.Context;
import magma.app.error.context.NodeContext;
import magma.app.error.context.StringContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public record OrRule(Stream<Rule> rules) implements Rule {
    private static <R, T> Result<R, List<CompileError>> fold(
            Result<R, List<CompileError>> previous,
            Supplier<Result<T, CompileError>> supplier,
            BiFunction<R, T, R> merger
    ) {
        return previous
                .and(() -> supplier.get().mapErr(Collections::singletonList))
                .mapValue(Tuple.merge(merger));
    }

    @Override
    public Result<Node, CompileError> parse(String value) {
        final var context = new StringContext(value);
        return this.complete(
                Optional.<Node>empty(), (rule) -> rule.parse(value), (optional, element) -> {
                    if (optional.isEmpty()) return Optional.of(element);
                    return optional;
                }, context
        ).flatMapValue(optional -> optional
                .<Result<Node, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("No rules present", context))));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return complete(
                new StringBuilder(),
                rule -> rule.generate(node),
                StringBuilder::append,
                new NodeContext(node)
        ).mapValue(StringBuilder::toString);
    }

    private <R, T> Result<R, CompileError> complete(
            R initial,
            Function<Rule, Result<T, CompileError>> performer,
            BiFunction<R, T, R> merger,
            Context context
    ) {
        return this.rules
                .<Result<R, List<CompileError>>>foldLeft(new Ok<>(initial),
                        (previous, next) -> fold(previous, () -> performer.apply(next), merger))
                .mapErr(errors -> new CompileError("Invalid combination", context, errors));
    }
}
