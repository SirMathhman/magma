package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class CommonLang {
    public static final String RETURN_TYPE = "return";
    public static final String RETURN_VALUE = "value";
    public static final String RETURN_PREFIX = "return ";
    public static final String STATEMENT_END = ";";
    public static final String CHILDREN = "children";
    public static final String DEFINITION_TYPE = "type";
    public static final String SYMBOL_TYPE = "symbol";
    public static final String SYMBOL_VALUE = "symbol-value";
    public static final String DECLARATION_TYPE = "declaration";
    public static final String DECLARATION_DEFINITION = CLang.DEFINITION;
    public static final String DECLARATION_AFTER_DEFINITION = "after-definition";
    public static final String DECLARATION_BEFORE_VALUE = "before-value";

    public static Rule createReturnRule() {
        final var value = new StringRule(RETURN_VALUE);
        return new TypeRule(RETURN_TYPE, new PrefixRule(RETURN_PREFIX, new SuffixRule(value, STATEMENT_END)));
    }

    public static TypeRule createSymbolTypeRule() {
        return new TypeRule(SYMBOL_TYPE, new StripRule(new StringRule(SYMBOL_VALUE)));
    }

    public static TypeRule createDeclarationRule(TypeRule definitionRule) {
        final var definition0 = new StripRule("", new NodeRule(DECLARATION_DEFINITION, definitionRule), DECLARATION_AFTER_DEFINITION);
        final var value = new StripRule(DECLARATION_BEFORE_VALUE, new NodeRule("value", createValueRule()), "");
        final var afterAssignment = new StripRule(new SuffixRule(value, STATEMENT_END));
        return new TypeRule(DECLARATION_TYPE, new FirstRule(definition0, "=", afterAssignment));
    }

    private static Rule createValueRule() {
        return new OrRule(List.of(
                new TypeRule(SYMBOL_TYPE, new StringRule(SYMBOL_VALUE))
        ));
    }
}
