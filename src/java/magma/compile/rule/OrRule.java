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
import magma.compile.error.NodeContext;
import magma.compile.error.StringContext;
import magma.java.JavaList;

import java.util.List;

public record OrRule(List<Rule> rules) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return Streams.from(rules)
                .foldLeft(new State(), (state, rule) -> state.foldCombination(rule, input))
                .complete(input);
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        for (Rule rule : rules) {
            final var generated = rule.generate(node);
            if (generated.isOk()) return generated;
        }

        return new Err<>(new CompileError("No valid combination", new NodeContext(node)));
    }

    private record State(Option<Node> maybePreviousNode, JavaList<CompileError> previousErrors) {
        public State() {
            this(new None<>(), new JavaList<>());
        }

        private State foldCombination(Rule rule, String input) {
            if (maybePreviousNode.isPresent()) return this;

            return rule.parse(input).match(
                    node -> new State(new Some<>(node), new JavaList<>()),
                    error -> new State(new None<>(), previousErrors.add(error))
            );
        }

        public Result<Node, CompileError> complete(String input) {
            return maybePreviousNode.<Result<Node, CompileError>>map(Ok::new).orElseGet(() -> {
                final var context = new StringContext(input);
                final var error = new CompileError("No valid combination", context, previousErrors);
                return new Err<>(error);
            });
        }
    }
}
