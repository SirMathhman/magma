package magma.rule;

import magma.Node;
import magma.error.NodeContext;
import magma.error.CompileError;
import magma.java.JavaList;
import magma.result.Err;
import magma.result.Result;
import magma.stream.ResultStream;
import magma.stream.Streams;

import java.util.ArrayList;
import java.util.List;

public record SplitRule(String propertyKey, Rule childRule) implements Rule {
    private static List<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            final var c = input.charAt(i);
            buffer.append(c);
            if (c == ';') {
                if (!buffer.isEmpty()) segments.add(buffer.toString());
                buffer = new StringBuilder();
            }
        }
        if (!buffer.isEmpty()) segments.add(buffer.toString());
        return segments;
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return Streams.from(split(input))
                .map(childRule::parse)
                .into(ResultStream::new)
                .foldResultsLeft(new JavaList<Node>(), JavaList::add)
                .mapValue(list -> new Node().withNodeList0(propertyKey, list));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        final var option = node.findNodeList(propertyKey);
        if (option.isEmpty()) return new Err<>(new CompileError("No children present", new NodeContext(node)));

        return option.orElse(new JavaList<>())
                .stream()
                .map(childRule::generate)
                .into(ResultStream::new)
                .foldResultsLeft(new StringBuilder(), StringBuilder::append)
                .mapValue(StringBuilder::toString);
    }
}
