package magma;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
}
