package magma;

import java.util.Arrays;

public record StringListRule(String propertyKey, String delimiter) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        final var namespace = Arrays.stream(input.split(delimiter)).toList();
        return new Ok<>(new Node().withStringList(propertyKey, namespace));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        final var namespace = node.findStringList(propertyKey);
        if (namespace.isEmpty())
            return new Err<>(new CompileError("String list '" + propertyKey + "' not present", new NodeContext(node)));

        return new Ok<>(String.join(this.delimiter(), namespace.get()));
    }
}