package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.app.compile.GenerateException;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

import java.util.Arrays;

public record StringListRule(String propertyKey, String delimiter) implements Rule {
    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        final var args = input.split(delimiter);
        if (args.length == 0) return new RuleResult<>(new Err<>(new ParseException("No items present", input)));

        return new RuleResult<>(new Ok<>(new MapNode().withStringList(propertyKey, Arrays.asList(args))));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        final var list = node.findStringList(propertyKey);
        if (list.isEmpty())
            return new RuleResult<>(new Err<>(new GenerateException("String list '" + propertyKey + "' not present", node)));

        final var joined = String.join(delimiter, list.get());
        return new RuleResult<>(new Ok<>(joined));
    }
}
