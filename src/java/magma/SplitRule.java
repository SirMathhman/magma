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

    @Override
    public Result<String, CompileException> generate(Node node) {
        return Streams.from(node.findNodeList(propertyKey()).orElse(Collections.emptyList()))
                .map(childRule()::generate)
                .into(ResultStream::new)
                .foldResultsLeft("", (current, next) -> current + next);
    }

    @Override
    public Result<Node, CompileException> parse(String input) {
        final var segments = Compiler.split(input);
        return Streams.from(segments)
                .map(childRule()::parse)
                .into(ResultStream::new)
                .foldResultsLeft(new ArrayList<>(), SplitRule::add)
                .mapValue(list -> new Node().withNodeList(propertyKey(), list));
    }
}