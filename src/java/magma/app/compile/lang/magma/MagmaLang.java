package magma.app.compile.lang.magma;

import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.rule.*;
import magma.java.JavaList;

public class MagmaLang {
    public static final String INITIALIZE_TYPE = "initialize";
    public static final String INITIALIZE_NAME = "name";
    public static final String INITIALIZE_TYPE_PROPERTY = "type";
    public static final String INITIALIZE_VALUE = "value";

    public static final String TUPLE_TYPE = "tuple";
    public static final String TUPLE_VALUES = "values";

    public static final String OUT_TYPE = "out";
    public static final String OUT_VALUE = "value";

    public static final String WHITESPACE_TYPE = "whitespace";

    public static final String CHAR_TYPE = "char";
    public static final String CHAR_VALUE = "value";

    public static final String SYMBOL_TYPE = "symbol";
    public static final String SYMBOL_VALUE = "value";

    public static final String ROOT_TYPE = "root";
    public static final String ROOT_CHILDREN = "children";

    public static final String BLOCK_TYPE = "block";
    public static final String BLOCK_CHILDREN = "children";

    public static Rule createMagmaRootRule() {
        return new TypeRule(ROOT_TYPE, new SplitRule(new BracketSplitter(), ROOT_CHILDREN, new OrRule(new JavaList<Rule>()
                .add(createWhitespaceRule())
                .add(createDeclarationRule())
                .add(createOutRule())
        )));
    }

    private static TypeRule createWhitespaceRule() {
        return new TypeRule(WHITESPACE_TYPE, new StripRule(new EmptyRule()));
    }

    private static TypeRule createOutRule() {
        final var value = new NodeRule(OUT_VALUE, createValueRule());
        return new TypeRule(OUT_TYPE, new StripRule(new PrefixRule("out ", new SuffixRule(value, ";"))));
    }

    private static TypeRule createDeclarationRule() {
        final var name = new StripRule(new StringRule(INITIALIZE_NAME));
        final var value = new StripRule(new NodeRule(INITIALIZE_VALUE, createValueRule()));

        final var definition = new PrefixRule("let ", new FirstRule(name, " : ", new NodeRule(INITIALIZE_TYPE_PROPERTY, createTypeRule())));
        final var afterAssign = new SuffixRule(value, ";");
        return new TypeRule(INITIALIZE_TYPE, new StripRule(new FirstRule(definition, "=", afterAssign)));
    }

    private static Rule createTypeRule() {
        return new TypeRule("numeric-type", new StripRule(new StringRule("value")));
    }

    private static Rule createValueRule() {
        final var value = new LazyRule();
        value.set(new OrRule(new JavaList<Rule>()
                .add(createTupleRule(value))
                .add(createAddRule(value))
                .add(createNumericRule())
                .add(createCharRule())
                .add(createSymbolRule())
        ));
        return value;
    }

    private static TypeRule createAddRule(LazyRule value) {
        return new TypeRule("add", new FirstRule(new NodeRule("left", value), "+", new NodeRule("right", value)));
    }

    private static TypeRule createSymbolRule() {
        final var value = new FilterRule(new SymbolFilter(), new StringRule(SYMBOL_VALUE));
        return new TypeRule(SYMBOL_TYPE, new StripRule(value));
    }

    private static TypeRule createCharRule() {
        final var value = new StringRule(CHAR_VALUE);
        return new TypeRule(CHAR_TYPE, new StripRule(new PrefixRule("'", new SuffixRule(value, "'"))));
    }

    private static TypeRule createNumericRule() {
        return new TypeRule(CommonLang.NUMERIC_VALUE_TYPE, new StripRule(new IntRule(CommonLang.NUMERIC_VALUE)));
    }

    private static TypeRule createTupleRule(LazyRule value) {
        final var childRule = new SplitRule(new ValueSplitter(), TUPLE_VALUES, value);
        return new TypeRule(TUPLE_TYPE, new PrefixRule("[", new SuffixRule(childRule, "]")));
    }
}
