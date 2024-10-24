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
        int i = 0;
        while (i < rules.size()) {
            Rule rule = rules.get(i);
            final var parsed = rule.parseWithTimeout(input);
            if (parsed.isValid()) return parsed;
            list.add(parsed);
            i++;
        }

        return new RuleResult<>(new Err<>(new ParseException("No valid rule in disjunction", input)), list);
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        var list = new ArrayList<RuleResult<String, GenerateException>>();
        int i = 0;
        while (i < rules.size()) {
            Rule rule = rules.get(i);
            final var generated = rule.generate(node);
            if (generated.isValid()) return generated;
            list.add(generated);
            i++;
        }
        return new RuleResult<>(new Err<>(new GenerateException("No valid rule in disjunction", node)), list);
    }
}
