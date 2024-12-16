package magma.app.rule;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.FormattedError;

public class DiscardRule implements Rule {
    @Override
    public Result<Node, FormattedError> parse(String input) {
        return new Ok<>(new MapNode());
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return new Ok<>("");
    }
}
