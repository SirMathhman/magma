package magma.compile.rule;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.Context;
import magma.compile.error.NodeContext;
import magma.compile.error.StringContext;
import magma.java.JavaList;

import java.util.List;
import java.util.function.Supplier;

public record OrRule(List<Rule> rules) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return Streams.from(rules)
                .foldLeft(new Accumulator<Node>(), (accumulator, rule) -> accumulator.foldCombination(() -> rule.parse(input)))
                .complete(() -> new StringContext(input));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return Streams.from(rules)
                .foldLeft(new Accumulator<String>(), (accumulator, rule) -> accumulator.foldCombination(() -> rule.generate(node)))
                .complete(() -> new NodeContext(node));
    }

    private record Accumulator<T>(Option<T> maybePreviousValue, JavaList<CompileError> previousErrors) {
        public Accumulator() {
            this(new None<>(), new JavaList<>());
        }

        private Accumulator<T> foldCombination(Supplier<Result<T, CompileError>> compute) {
            if (maybePreviousValue.isPresent()) return this;

            return compute.get().match(
                    value -> new Accumulator<>(new Some<>(value), new JavaList<>()),
                    error -> new Accumulator<>(new None<>(), previousErrors.add(error))
            );
        }

        public Result<T, CompileError> complete(Supplier<Context> contextFactory) {
            return maybePreviousValue.<Result<T, CompileError>>map(Ok::new).orElseGet(() -> {
                final var context = contextFactory.get();
                final var error = new CompileError("No valid combination", context, previousErrors);
                return new Err<>(error);
            });
        }
    }
}
