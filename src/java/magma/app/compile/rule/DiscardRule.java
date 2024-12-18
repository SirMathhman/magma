package magma.app.compile.rule;

import magma.app.Input;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.FormattedError;

public class DiscardRule implements Rule {
    private Result<Node, FormattedError> parse0(String input) {
        return new Ok<>(new MapNode());
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return new Ok<>("");
    }

    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return parse0(input.slice());
    }
}
