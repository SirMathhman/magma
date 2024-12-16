package magma;

import magma.error.CompileError;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.util.ArrayList;
import java.util.function.BiFunction;

public record SplitRule(String propertyKey, Rule segmentRule) implements Rule {

    private static ArrayList<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    @Override
    public Result<Node, CompileError> parse(String root) {
        return Streams.from(split(root))
                .<Result<List<Node>, CompileError>>foldLeft(new Ok<>(new MutableList<>()), (current, s) -> current.flatMapValue(inner -> segmentRule().parse(s).mapValue(inner::add)))
                .mapValue(nodes -> new MapNode().withNodeList(propertyKey(), nodes));
    }


    @Override
    public Result<String, CompileError> generate(Node node) {
        return node.findNodeList(propertyKey())
                .map(list -> list.stream()
                        .foldLeft(new Ok<>(new StringBuilder()), (BiFunction<Result<StringBuilder, CompileError>, Node, Result<StringBuilder, CompileError>>)
                                (current, next) -> current.flatMapValue(inner -> segmentRule().generate(next).mapValue(inner::append)))
                        .mapValue(StringBuilder::toString))
                .orElseGet(() -> new Err<>(new CompileError("Node list '" + propertyKey() + "' not present", new NodeContext(node))));
    }
}