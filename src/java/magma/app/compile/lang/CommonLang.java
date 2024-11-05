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
    public static final String NUMBER_VALUE = "value";
    public static final String NUMBER_TYPE = "number";
    public static final String OPERATION_AFTER_LEFT = "after-left";
    public static final String OPERATION_BEFORE_RIGHT = "before-right";
    public static final String DECLARATION_VALUE = "value";
    public static final String ADD_TYPE = "add";
    public static final String FUNCTION_TYPE = "function";
    public static final String FUNCTION_NAME = "name";

    public static Rule createReturnRule() {
        final var value = new NodeRule(RETURN_VALUE, createValueRule());
        return new TypeRule(RETURN_TYPE, new PrefixRule(RETURN_PREFIX, new SuffixRule(value, STATEMENT_END)));
    }

    public static TypeRule createSymbolTypeRule() {
        return new TypeRule(SYMBOL_TYPE, new StripRule(new StringRule(SYMBOL_VALUE)));
    }

    public static TypeRule createDeclarationRule(TypeRule definitionRule) {
        final var definition0 = new StripRule("", new NodeRule(DECLARATION_DEFINITION, definitionRule), DECLARATION_AFTER_DEFINITION);
        final var value = new StripRule(DECLARATION_BEFORE_VALUE, new NodeRule(DECLARATION_VALUE, createValueRule()), "");
        final var afterAssignment = new StripRule(new SuffixRule(value, STATEMENT_END));
        return new TypeRule(DECLARATION_TYPE, new FirstRule(definition0, "=", afterAssignment));
    }

    public static Rule createValueRule() {
        final var value = new LazyRule();
        value.setRule(new OrRule(List.of(
                createOperationRule(value),
                new TypeRule(NUMBER_TYPE, new StripRule(new FilterRule(new NumberFilter(), new StringRule(NUMBER_VALUE)))),
                new TypeRule(SYMBOL_TYPE, new StripRule(new FilterRule(new SymbolFilter(), new StringRule(SYMBOL_VALUE))))
        )));
        return value;
    }

    private static TypeRule createOperationRule(Rule value) {
        final var left = new StripRule("before-left", new NodeRule("left", value), OPERATION_AFTER_LEFT);
        final var right = new StripRule(OPERATION_BEFORE_RIGHT, new NodeRule("right", value), "after-right");
        return new TypeRule(ADD_TYPE, new FirstRule(left, "+", right));
    }
}
