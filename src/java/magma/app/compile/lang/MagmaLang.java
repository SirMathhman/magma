package magma.app.compile.lang;

import magma.app.compile.rule.*;
import magma.java.JavaList;

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
    public static final String TUPLE_TYPE = "tuple";
    public static final String TUPLE_VALUES = "values";
    public static final String REFERENCE_TYPE = "reference";
    public static final String REFERENCE_VALUE = "value";
    public static final String INDEX_TYPE = "index";
    public static final String INDEX_VALUE = "value";
    public static final String INDEX_OFFSET = "offset";

    public static Rule createMagmaRootRule() {
        final List<Rule> function = List.of(
                createDeclarationRule(),
                createReturnRule(),
                new TypeRule("function", new StripRule(new PrefixRule("def empty() => {}", new EmptyRule())))
        );
        return new TypeRule(ROOT_TYPE, new NodeListRule(new BracketSplitter(), "children", new StripRule(new OrRule(new JavaList<>(function)))));
    }

    private static TypeRule createDeclarationRule() {
        final var value = new NodeRule(DECLARATION_VALUE, createValueRule());
        final var name = new StripRule(new StringRule(DECLARATION_NAME));
        final var definition = new PrefixRule("let ", name);
        return new TypeRule(DECLARATION_TYPE, new FirstRule(new FirstLocator("="), definition, "=", new SuffixRule(value, ";")));
    }

    private static Rule createValueRule() {
        final var value = new LazyRule();
        final List<Rule> numberRule = List.of(
                createNumberRule(),
                createSymbolRule(),
                createAddRule(value),
                createArrayRule(value),
                createIndexRule(value),
                new TypeRule(REFERENCE_TYPE, new StripRule(new PrefixRule("&", new NodeRule(REFERENCE_VALUE, value))))
        );
        value.setRule(new OrRule(new JavaList<>(numberRule)));
        return value;
    }

    private static TypeRule createIndexRule(LazyRule value) {
        final var indexValue = new NodeRule(INDEX_VALUE, value);
        final var indexOffset = new NodeRule(INDEX_OFFSET, value);
        return new TypeRule(INDEX_TYPE, new FirstRule(new LastLocator("["), indexValue, "[", new StripRule(new SuffixRule(indexOffset, "]"))));
    }

    private static TypeRule createArrayRule(LazyRule value) {
        final var values = new NodeListRule(new ValueSplitter(), TUPLE_VALUES, value);
        return new TypeRule(TUPLE_TYPE, new StripRule(new PrefixRule("[", new SuffixRule(values, "]"))));
    }

    private static TypeRule createAddRule(Rule value) {
        return new TypeRule(ADD_TYPE, new FirstRule(new FirstLocator("+"), new NodeRule(ADD_LEFT, value), "+", new NodeRule(ADD_RIGHT, value)));
    }

    private static TypeRule createSymbolRule() {
        return new TypeRule(SYMBOL_TYPE, new StripRule(new FilterRule(new SymbolFilter(), new StringRule(SYMBOL_VALUE))));
    }

    private static Rule createNumberRule() {
        return new TypeRule(NUMERIC_TYPE, new StripRule(new FilterRule(new NumberFilter(), new IntRule(NUMERIC_VALUE))));
    }

    private static Rule createReturnRule() {
        final var value = new NodeRule(RETURN_VALUE, createValueRule());

        final var withValue = new PrefixRule("return ", new SuffixRule(value, ";"));
        final var withoutValue = new StripRule(new PrefixRule("return;", new EmptyRule()));

        final var rules = new JavaList<Rule>()
                .addLast(withValue)
                .addLast(withoutValue);

        return new TypeRule(RETURN_TYPE, new OrRule(rules));
    }
}
