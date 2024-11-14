package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class MagmaLang {

    public static final String RETURN_VALUE = "value";
    public static final String DECLARATION_TYPE = "declaration";
    public static final String RETURN_TYPE = "return";
    public static final String ROOT_TYPE = "root";
    public static final String DECLARATION_VALUE = "value";
    public static final String NUMERIC_TYPE = "number";
    public static final String NUMERIC_VALUE = "value";
    public static final String SYMBOL_TYPE = "symbol";
    public static final String SYMBOL_VALUE = "value";
    public static final String DECLARATION_NAME = "name";
    public static final String ADD_TYPE = "add";
    public static final String ADD_LEFT = "left";
    public static final String ADD_RIGHT = "right";
    public static final String ARRAY_TYPE = "array";
    public static final String ARRAY_VALUES = "values";
    public static final String REFERENCE_TYPE = "reference";
    public static final String REFERENCE_VALUE = "value";
    public static final String INDEX_TYPE = "index";
    public static final String INDEX_VALUE = "value";
    public static final String INDEX_OFFSET = "offset";

    public static Rule createMagmaRootRule() {
        return new TypeRule(ROOT_TYPE, new NodeListRule(new BracketSplitter(), "children", new StripRule(new OrRule(List.of(
                createDeclarationRule(),
                createReturnRule(),
                new TypeRule("function", new StripRule(new PrefixRule("def empty() => {}", new EmptyRule())))
        )))));
    }

    private static TypeRule createDeclarationRule() {
        final var value = new NodeRule(DECLARATION_VALUE, createValueRule());
        final var name = new StripRule(new StringRule(DECLARATION_NAME));
        final var definition = new PrefixRule("let ", name);
        return new TypeRule(DECLARATION_TYPE, new FirstRule(definition, "=", new SuffixRule(value, ";")));
    }

    private static Rule createValueRule() {
        final var value = new LazyRule();
        value.setRule(new OrRule(List.of(
                createNumberRule(),
                createSymbolRule(),
                createAddRule(value),
                createArrayRule(value),
                createIndexRule(value),
                new TypeRule(REFERENCE_TYPE, new StripRule(new PrefixRule("&", new NodeRule(REFERENCE_VALUE, value))))
        )));
        return value;
    }

    private static TypeRule createIndexRule(LazyRule value) {
        final var indexValue = new NodeRule(INDEX_VALUE, value);
        final var indexOffset = new NodeRule(INDEX_OFFSET, value);
        return new TypeRule(INDEX_TYPE, new FirstRule(indexValue, "[", new StripRule(new SuffixRule(indexOffset, "]"))));
    }

    private static TypeRule createArrayRule(LazyRule value) {
        final var values = new NodeListRule(new ValueSplitter(), ARRAY_VALUES, value);
        return new TypeRule(ARRAY_TYPE, new StripRule(new PrefixRule("[", new SuffixRule(values, "]"))));
    }

    private static TypeRule createAddRule(Rule value) {
        return new TypeRule(ADD_TYPE, new FirstRule(new NodeRule(ADD_LEFT, value), "+", new NodeRule(ADD_RIGHT, value)));
    }

    private static TypeRule createSymbolRule() {
        return new TypeRule(SYMBOL_TYPE, new StripRule(new FilterRule(new SymbolFilter(), new StringRule(SYMBOL_VALUE))));
    }

    private static Rule createNumberRule() {
        return new TypeRule(NUMERIC_TYPE, new StripRule(new FilterRule(new NumberFilter(), new IntRule(NUMERIC_VALUE))));
    }

    private static Rule createReturnRule() {
        final var value = new NodeRule(RETURN_VALUE, createValueRule());
        return new TypeRule(RETURN_TYPE, new PrefixRule("return ", new SuffixRule(value, ";")));
    }
}
