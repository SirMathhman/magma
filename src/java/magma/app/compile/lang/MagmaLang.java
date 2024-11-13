package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class MagmaLang {

    public static final String RETURN_VALUE = "value";
    public static final String DECLARATION_TYPE = "declaration";
    public static final String RETURN_TYPE = "return";
    public static final String ROOT_TYPE = "root";
    public static final String DECLARATION_VALUE = "value";
    public static final String NUMBER_TYPE = "number";
    public static final String NUMBER_VALUE = "value";
    public static final String SYMBOL_TYPE = "symbol";
    public static final String SYMBOL_VALUE = "value";

    public static Rule createMagmaRootRule() {
        return new TypeRule(ROOT_TYPE, new NodeListRule("children", new StripRule(new OrRule(List.of(
                createDeclarationRule(),
                createReturnRule()
        )))));
    }

    private static TypeRule createDeclarationRule() {
        final var value = new NodeRule(DECLARATION_VALUE, createValueRule());
        final var name = new StripRule(new StringRule("name"));
        final var definition = new PrefixRule("let ", name);
        return new TypeRule(DECLARATION_TYPE, new FirstRule(definition, "=", new SuffixRule(value, ";")));
    }

    private static Rule createValueRule() {
        return new OrRule(List.of(
                createNumberRule(),
                createSymbolRule()
        ));
    }

    private static TypeRule createSymbolRule() {
        return new TypeRule(SYMBOL_TYPE, new FilterRule(new SymbolFilter(), new StringRule(SYMBOL_VALUE)));
    }

    private static Rule createNumberRule() {
        return new TypeRule(NUMBER_TYPE, new StripRule(new FilterRule(new NumberFilter(), new IntRule(NUMBER_VALUE))));
    }

    private static Rule createReturnRule() {
        final var value = new NodeRule(RETURN_VALUE, createValueRule());
        return new TypeRule(RETURN_TYPE, new PrefixRule("return ", new SuffixRule(value, ";")));
    }
}
