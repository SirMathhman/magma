package magma.app.rule;package magma.api.result.Err;package magma.api.result.Result;package magma.api.stream.Streams;package magma.app.Node;package magma.app.error.CompileError;package magma.app.error.context.Context;package magma.app.error.context.NodeContext;package magma.app.error.context.StringContext;package java.util.ArrayList;package java.util.Collections;package java.util.List;package java.util.function.Function;public record OrRule(List<Rule> rules) implements Rule {@Override
    public Result<Node, CompileError> parse(String value){return process(new StringContext(value), rule -> rule.parse(value));}private <R> Result<R, CompileError> process(Context contextContext context Function<Rule, Result<R, CompileError>> mapper){return Streams.from(this.rules)
                .map(rule -> mapper.apply(rule).mapErr(Collections::singletonList))
                .foldLeft((first, second) -> first.or(() -> second).mapErr(tuple -> {
                    final var left = new ArrayList<>(tuple.left());
                    left.addAll(tuple.right());
                    return left;
                }))
                .orElseGet(() -> new Err<>(Collections.singletonList(new CompileError("No rules set", context))))
                .mapErr(errors -> new CompileError("No valid rule", context, errors));}@Override
    public Result<String, CompileError> generate(Node node){return process(new NodeContext(node), rule -> rule.generate(node));}}