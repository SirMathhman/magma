package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.java.JavaList;
import magma.java.JavaStreams;

import java.util.ArrayList;
import java.util.List;

public class NodeListRule implements Rule {
    private final String propertyKey;
    private final Rule segmentRule;

    public NodeListRule(String propertyKey, Rule segmentRule) {
        this.segmentRule = segmentRule;
        this.propertyKey = propertyKey;
    }

    private static Result<String, CompileError> foldIntoString(
            Result<String, CompileError> current,
            Result<String, CompileError> next
    ) {
        return current.and(() -> next)
                .mapValue(tuple -> tuple.left() + tuple.right());
    }

    private static Result<List<Node>, CompileError> foldIntoList(
            Result<List<Node>, CompileError> current,
            Result<Node, CompileError> next
    ) {
        return current.and(() -> next).mapValue(tuple -> {
            final var copy = tuple.left();
            copy.add(tuple.right());
            return copy;
        });
    }

    private static List<String> split(String input) {
        return JavaStreams.fromString(input)
                .foldLeft(new SplitState(), NodeListRule::splitAtChar)
                .advance()
                .segments();
    }

    private static SplitState splitAtChar(SplitState splitSTate, char c) {
        final var appended = splitSTate.append(c);
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();

        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        final var segments = split(input);

        return segments.stream()
                .map(segmentRule::parse)
                .reduce(new Ok<>(new ArrayList<>()), NodeListRule::foldIntoList, (_, next) -> next)
                .mapValue(list -> new MapNode().withNodeList(propertyKey, list));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return node.findNodeList(propertyKey).map(JavaList::list)
                .map(this::generateList)
                .orElseGet(() -> new Err<>(new CompileError("Node list property 'children' not present", new NodeContext(node))));
    }

    private Result<String, CompileError> generateList(List<Node> list) {
        return list.stream()
                .map(segmentRule::generate)
                .reduce(new Ok<>(""), NodeListRule::foldIntoString, (_, next) -> next);
    }
}
