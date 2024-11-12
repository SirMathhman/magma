package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class MagmaLang {
    public static final String RETURN_VALUE = "value";
    public static final String NUMBER_VALUE = "value";

    public static Rule createMagmaRootRule() {
        final var value = new NodeRule(RETURN_VALUE, createNumberRule());
        return new TypeRule("return", new PrefixRule("return", new SuffixRule(new OrRule(List.of(
                new PrefixRule(" ", value),
                new EmptyRule()
        )), ";")));
    }

    private static TypeRule createNumberRule() {
        return new TypeRule("number", new FilterRule(new NumberFilter(), new IntRule(NUMBER_VALUE)));
    }
}
