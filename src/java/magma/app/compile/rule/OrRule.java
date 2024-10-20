package magma.app.compile.rule;

import magma.api.result.Err;
import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

import java.util.ArrayList;
import java.util.List;

public record OrRule(List<Rule> rules) implements Rule {
    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        var list = new ArrayList<RuleResult<Node, ParseException>>();
        for (Rule rule : rules) {
            final var parsed = rule.parse(input);
            if (parsed.isValid()) return parsed;
            list.add(parsed);
        }

        return new RuleResult<>(new Err<>(new ParseException("No valid rule in disjunction", input)), list);
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        var list = new ArrayList<RuleResult<String, GenerateException>>();
        for (Rule rule : rules) {
            final var generated = rule.generate(node);
            if (generated.isValid()) return generated;
            list.add(generated);
        }
        return new RuleResult<>(new Err<>(new GenerateException("No valid rule in disjunction", node)), list);
    }
}
