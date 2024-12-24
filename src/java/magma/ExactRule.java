package magma;

import java.util.Optional;

public record ExactRule(String slice) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (input.equals(slice)) return new Ok<>(new Node());
        return new Err<>(new CompileError("Exact slice '" + slice + "' not present", new StringContext(input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return new Ok<>(slice);
    }
}
