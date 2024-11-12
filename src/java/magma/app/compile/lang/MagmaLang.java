package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class MagmaLang {
    public static final String RETURN_VALUE = "value";
    public static final String NUMBER_VALUE = "value";
    public static final String ROOT_TYPE = "root";
    public static final String ROOT_CHILDREN = "children";
    public static final String RETURN_TYPE = "return";
    public static final String DECLARATION_TYPE = "declaration";
    public static final String DECLARATION_VALUE = "value";

    public static Rule createMagmaRootRule() {
        return new TypeRule(ROOT_TYPE, new NodeListRule(ROOT_CHILDREN, new StripRule(new OrRule(List.of(
                createDeclarationRule(),
                createReturnRule()
        )))));
    }

    private static TypeRule createDeclarationRule() {
        final var name = new StripRule(new FilterRule(new SymbolFilter(), new StringRule("name")));
        final var value = new NodeRule(DECLARATION_VALUE, createValueRule());
        return new TypeRule(DECLARATION_TYPE, new PrefixRule("let ", new SuffixRule(new FirstRule(name, "=", value), ";")));
    }

    private static TypeRule createReturnRule() {
        final var value = new NodeRule(RETURN_VALUE, createValueRule());
        return new TypeRule(RETURN_TYPE, new PrefixRule("return", new SuffixRule(new OrRule(List.of(
                new PrefixRule(" ", value),
                new EmptyRule()
        )), ";")));
    }

    private static Rule createValueRule() {
        return new OrRule(List.of(
                createSymbolRule(),
                createNumberRule()
        ));
    }

    private static TypeRule createSymbolRule() {
        return new TypeRule("symbol", new StripRule(new FilterRule(new SymbolFilter(), new StringRule("value"))));
    }

    private static TypeRule createNumberRule() {
        return new TypeRule("number", new StripRule(new FilterRule(new NumberFilter(), new IntRule(NUMBER_VALUE))));
    }
}
