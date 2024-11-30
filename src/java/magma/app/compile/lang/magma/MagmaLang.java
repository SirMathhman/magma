package magma.app.compile.lang.magma;

import magma.app.compile.rule.*;
import magma.java.JavaList;

public class MagmaLang {
    public static final String INT_TYPE = "int";
    public static final String INT_VALUE = "value";
    public static final String DECLARATION_TYPE = "declaration";
    public static final String DECLARATION_TYPE_PROPERTY = "type";
    public static final String DECLARATION_VALUE = "value";

    public static final String TUPLE_TYPE = "tuple";
    public static final String TUPLE_VALUES = "values";
    public static final String DECLARATION_NAME = "name";
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
        final var name = new StripRule(new StringRule(DECLARATION_NAME));
        final var value = new StripRule(new NodeRule(DECLARATION_VALUE, createValueRule()));

        final var beforeAssign = new PrefixRule("let ", name);
        final var afterAssign = new SuffixRule(value, ";");
        return new TypeRule(DECLARATION_TYPE, new StripRule(new FirstRule(beforeAssign, "=", afterAssign)));
    }

    private static Rule createValueRule() {
        final var value = new LazyRule();
        value.set(new OrRule(new JavaList<Rule>()
                .add(createTupleRule(value))
                .add(createIntType())
                .add(createCharRule())
                .add(createSymbolRule())
        ));
        return value;
    }

    private static TypeRule createSymbolRule() {
        return new TypeRule(SYMBOL_TYPE, new StripRule(new StringRule(SYMBOL_VALUE)));
    }

    private static TypeRule createCharRule() {
        final var value = new StringRule(CHAR_VALUE);
        return new TypeRule(CHAR_TYPE, new StripRule(new PrefixRule("'", new SuffixRule(value, "'"))));
    }

    private static TypeRule createIntType() {
        return new TypeRule(INT_TYPE, new StripRule(new IntRule(INT_VALUE)));
    }

    private static TypeRule createTupleRule(LazyRule value) {
        final var childRule = new SplitRule(new ValueSplitter(), TUPLE_VALUES, value);
        return new TypeRule(TUPLE_TYPE, new PrefixRule("[", new SuffixRule(childRule, "]")));
    }
}
