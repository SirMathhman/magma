package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;

import java.util.ArrayList;
import java.util.List;

public class SplitRule implements Rule {
    private final Rule segmentRule;

    public SplitRule(Rule segmentRule) {
        this.segmentRule = segmentRule;
    }

    private static Result<String, CompileError> foldIntoString(
            Result<String, CompileError> current,
            Result<String, CompileError> next
    ) {
        return current.and(() -> next)
                .mapValue(tuple -> tuple.left() + tuple.right());
    }

    private static StringBuilder advance(ArrayList<String> list, StringBuilder buffer) {
        if (!buffer.isEmpty()) list.add(buffer.toString());
        buffer = new StringBuilder();
        return buffer;
    }

    private static Node wrap(List<Node> list) {
        return new MapNode()
                .withNodeList("children", list)
                .orElse(new MapNode());
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
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            buffer.append(c);
            if (c == ';') {
                buffer = advance(segments, buffer);
            }
        }

        advance(segments, buffer);
        return segments;
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        final var segments = split(input);

        return segments.stream()
                .map(segmentRule::parse)
                .reduce(new Ok<>(new ArrayList<>()), SplitRule::foldIntoList, (_, next) -> next)
                .mapValue(SplitRule::wrap);
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return node.findNodeList("children")
                .map(this::generateList)
                .orElseGet(() -> new Err<>(new CompileError("Node list property 'children' not present", new NodeContext(node))));
    }

    private Result<String, CompileError> generateList(List<Node> list) {
        return list.stream()
                .map(segmentRule::generate)
                .reduce(new Ok<>(""), SplitRule::foldIntoString, (_, next) -> next);
    }
}
