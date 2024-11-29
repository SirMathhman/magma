package magma.app.compile.rule;

import magma.app.compile.Node;
import magma.app.compile.CompileError;
import magma.app.error.NodeContext;
import magma.java.JavaList;
import magma.api.result.Err;
import magma.api.result.Result;
import magma.api.stream.ResultStream;
import magma.api.stream.Streams;

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
        return Streams.from(splitter.split(input))
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
