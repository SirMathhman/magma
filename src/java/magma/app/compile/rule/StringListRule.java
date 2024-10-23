package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.app.compile.GenerateException;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

import java.util.ArrayList;
import java.util.List;

public record StringListRule(String propertyKey, String delimiter) implements Rule {
    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        final var args = split(input)
                .stream()
                .filter(value -> !value.isEmpty())
                .toList();

        if (args.isEmpty()) return new RuleResult<>(new Err<>(new ParseException("No items present", input)));

        return new RuleResult<>(new Ok<>(new MapNode().withStringList(propertyKey, args)));
    }

    private List<String> split(String input) {
        String remaining = input;
        List<String> parts = new ArrayList<>();

        while (true) {
            var index = remaining.indexOf(delimiter);
            if (index == -1) break;

            parts.add(remaining.substring(0, index));
            remaining = remaining.substring(index + delimiter.length());
        }
        parts.add(remaining);
        return parts;
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
