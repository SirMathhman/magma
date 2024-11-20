package magma;

import magma.result.Result;
import magma.rule.Rule;
import magma.stream.ResultStream;
import magma.stream.Streams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record SplitRule(String propertyKey, Rule childRule) implements Rule {
    static List<Node> add(List<Node> current, Node element) {
        final var copy = new ArrayList<>(current);
        copy.add(element);
        return copy;
    }

    static ArrayList<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            buffer.append(c);
            if (c == ';') {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    @Override
    public Result<String, CompileException> generate(Node node) {
        return Streams.from(node.findNodeList(propertyKey()).orElse(Collections.emptyList()))
                .map(childRule()::generate)
                .into(ResultStream::new)
                .foldResultsLeft("", (current, next) -> current + next);
    }

    @Override
    public Result<Node, CompileException> parse(String input) {
        final var segments = split(input);
        return Streams.from(segments)
                .map(childRule()::parse)
                .into(ResultStream::new)
                .foldResultsLeft(new ArrayList<>(), SplitRule::add)
                .mapValue(list -> new Node().withNodeList(propertyKey(), list));
    }
}