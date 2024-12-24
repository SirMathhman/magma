package magma;

import java.util.Optional;

public class DiscardRule implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return new Ok<>(new Node());
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return new Ok<>("");
    }
}
