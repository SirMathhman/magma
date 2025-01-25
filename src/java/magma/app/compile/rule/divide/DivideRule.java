package magma.app.compile.rule.divide;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.compile.node.MapNode;
import magma.app.compile.node.Node;
import magma.app.compile.node.NodeProperties;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
import magma.app.compile.rule.Rule;
import magma.java.JavaList;
import magma.java.JavaOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DivideRule implements Rule {
    private final String propertyKey;
    private final Divider divider;
    private final Rule childRule;

    public DivideRule(String propertyKey, Divider divider, Rule childRule) {
        this.divider = divider;
        this.childRule = childRule;
        this.propertyKey = propertyKey;
    }

    public static <T, R> Result<List<R>, CompileError> compileAll(
            List<T> segments,
            Function<T, Result<R, CompileError>> mapper,
            BiFunction<T, R, Result<R, CompileError>> validator
    ) {
        return Streams.fromNativeList(segments).foldLeftToResult(new ArrayList<>(), (rs, t) -> {
            return mapper.apply(t)
                    .flatMapValue(inner -> validator.apply(t, inner))
                    .mapValue(inner -> {
                        rs.add(inner);
                        return rs;
                    });
        });
    }

    private static Result<Node, CompileError> validateNode(String text, Node result) {
        if (result.hasType()) {
            return new Ok<>(result);
        } else {
            return new Err<>(new CompileError("Node has no type assigned", new NodeContext(result)));
        }
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return this.divider.divide(input)
                .flatMapValue(segments -> compileAll(segments, this.childRule::parse, DivideRule::validateNode))
                .mapValue(segments -> {
                    final var node = new MapNode();
                    if (segments.isEmpty()) {
                        return node;
                    } else {
                        NodeProperties<JavaList<Node>> javaListNodeProperties = node.nodeLists();
                        return javaListNodeProperties.with(this.propertyKey, new JavaList<Node>(segments)).orElse(new MapNode());
                    }
                });
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return JavaOptions.toNative(node.nodeLists().find(this.propertyKey).map(JavaList::list))
                .flatMap(list -> list.isEmpty() ? Optional.empty() : Optional.of(list))
                .map(list -> compileAll(list, this.childRule::generate, (_, result) -> new Ok<>(result)))
                .map(result -> result.mapValue(this::merge))
                .orElseGet(() -> new Err<>(new CompileError("Node list '" + this.propertyKey + "' not present", new NodeContext(node))));
    }

    private String merge(List<String> elements) {
        return Streams.fromNativeList(elements)
                .foldLeft(this.divider::merge)
                .orElse("");
    }
}
