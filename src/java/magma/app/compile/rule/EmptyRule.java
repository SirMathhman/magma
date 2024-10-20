package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.app.compile.GenerateException;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

public class EmptyRule implements Rule {
    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        return input.isEmpty()
                ? new RuleResult<>(new Ok<>(new MapNode()))
                : new RuleResult<>(new Err<>(new ParseException("Input is not empty", input)));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        return new RuleResult<>(new Ok<>(""));
    }
}
