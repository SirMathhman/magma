package magma.app.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;

import java.util.ArrayList;
import java.util.List;
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
            Function<T, Result<R, CompileError>> mapper
    ) {
        return Streams.from(segments).foldLeftToResult(new ArrayList<>(), (rs, t) -> mapper.apply(t).mapValue(inner -> {
            rs.add(inner);
            return rs;
        }));
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return this.divider.divide(input)
                .flatMapValue(segments -> compileAll(segments, this.childRule::parse))
                .mapValue(segments -> new MapNode().withNodeList(this.propertyKey, segments));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return node.findNodeList(this.propertyKey)
                .map(list -> compileAll(list, this.childRule::generate))
                .map(result -> result.mapValue(list -> String.join("", list)))
                .orElseGet(() -> new Err<>(new CompileError("Node list '" + this.propertyKey + "' not present", new NodeContext(node))));
    }
}
