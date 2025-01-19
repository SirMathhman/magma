package magma;

import magma.app.rule.Rule;

public record NodeRule(String propertyKey, Rule childRule) implements Rule {
}
