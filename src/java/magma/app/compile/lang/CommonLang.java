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

    public static Rule createReturnRule() {
        final var value = new StringRule(RETURN_VALUE);
        return new TypeRule(RETURN_TYPE, new PrefixRule(RETURN_PREFIX, new SuffixRule(value, STATEMENT_END)));
    }

    public static TypeRule createSymbolTypeRule() {
        return new TypeRule(SYMBOL_TYPE, new StripRule(new StringRule(SYMBOL_VALUE)));
    }

    public static TypeRule createDeclarationRule(TypeRule definition) {
        final var afterAssignment = new StripRule(new SuffixRule(new NodeRule("value", createValueRule()), ";"));
        return new TypeRule(DECLARATION_TYPE, new FirstRule(new NodeRule(DECLARATION_DEFINITION, definition), "=", afterAssignment));
    }

    private static Rule createValueRule() {
        return new OrRule(List.of(
                new TypeRule(SYMBOL_TYPE, new StringRule(SYMBOL_VALUE))
        ));
    }
}
