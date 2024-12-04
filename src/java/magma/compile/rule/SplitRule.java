package magma.compile.rule;

import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.java.JavaList;

public final class SplitRule implements Rule {
    private final Rule childRule;
    private final String propertyKey;
    private final Splitter splitter;

    public SplitRule(Splitter splitter, String propertyKey, Rule childRule) {
        this.childRule = childRule;
        this.propertyKey = propertyKey;
        this.splitter = splitter;
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return Streams.from(splitter.split(input))
                .foldLeftIntoResult(new JavaList<Node>(),
                        (list, segment) -> childRule.parse(segment).mapValue(list::add))
                .mapValue(list -> new Node().withNodeList(propertyKey, list));
    }

    @Override
    public Result<String, CompileError> generate(Node value) {
        return value.findNodeList(propertyKey)
                .orElse(new JavaList<>())
                .stream().foldLeftIntoResult(new StringBuilder(),
                        (builder, segment) -> childRule.generate(segment).mapValue(builder::append))
                .mapValue(StringBuilder::toString);
    }
}