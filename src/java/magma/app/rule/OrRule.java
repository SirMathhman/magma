package magma.app.rule;

import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.Context;
import magma.app.error.context.NodeContext;
import magma.app.error.context.StringContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public record OrRule(List<Rule> rules) implements Rule {
    private static List<CompileError> join(Tuple<List<CompileError>, List<CompileError>> tuple) {
        final var left = new ArrayList<>(tuple.left());
        left.addAll(tuple.right());
        return left;
    }

    @Override
    public Result<Node, CompileError> parse(String value) {
        return process(new StringContext(value), rule -> rule.parse(value));
    }

    private <R> Result<R, CompileError> process(Context context, Function<Rule, Result<R, CompileError>> mapper) {
        return Streams.fromNativeList(this.rules)
                .map(rule -> wrapResultInList(mapper, rule))
                .foldLeft(OrRule::join)
                .orElseGet(() -> createError(context))
                .mapErr(errors -> new CompileError("No valid rule", context, errors));
    }

    private static <R> Result<R, List<CompileError>> join(
            Result<R, List<CompileError>> first,
            Result<R, List<CompileError>> second
    ) {
        return first.or(() -> second).mapErr(OrRule::join);
    }

    private static <R> Result<R, List<CompileError>> wrapResultInList(Function<Rule, Result<R, CompileError>> mapper, Rule rule) {
        return mapper.apply(rule).mapErr(Collections::singletonList);
    }

    private static <R> Result<R, List<CompileError>> createError(Context context) {
        return new Err<>(Collections.singletonList(new CompileError("No rules set", context)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return process(new NodeContext(node), rule -> rule.generate(node));
    }
}
