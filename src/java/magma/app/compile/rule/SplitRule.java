package magma.app.compile.rule;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.java.JavaList;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.api.stream.ResultStream;

import java.util.List;

public final class SplitRule implements Rule {
    private final String propertyKey;
    private final Rule childRule;
    private final Splitter splitter;

    public SplitRule(Splitter splitter, String propertyKey, Rule childRule) {
        this.propertyKey = propertyKey;
        this.childRule = childRule;
        this.splitter = splitter;
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        List<String> list1 = splitter.split(input);
        return new JavaList<>(list1).stream()
                .map(childRule::parse)
                .into(ResultStream::new)
                .foldResultsLeft(new JavaList<Node>(), JavaList::add)
                .mapValue(list -> new MapNode().withNodeList(propertyKey, list));
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
