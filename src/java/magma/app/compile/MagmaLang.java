package magma.app.compile;

import magma.app.compile.rule.*;
import magma.java.JavaList;

public class MagmaLang {
    public static final String INT_TYPE = "int";
    public static final String INT_VALUE = "value";
    public static final String DECLARATION_TYPE = "declaration";
    public static final String DECLARATION_VALUE = "value";
    public static final String TUPLE_TYPE = "tuple";
    public static final String TUPLE_VALUES = "values";
    public static final String DECLARATION_NAME = "name";

    public static Rule createMagmaRootRule() {
        return new SplitRule(new BracketSplitter(), "children", createDeclarationRule());
    }

    private static TypeRule createDeclarationRule() {
        final var name = new StripRule(new StringRule(DECLARATION_NAME));
        final var value = new StripRule(new NodeRule(DECLARATION_VALUE, createValueRule()));

        final var beforeAssign = new PrefixRule("let ", name);
        final var afterAssign = new SuffixRule(value, ";");
        return new TypeRule(DECLARATION_TYPE, new FirstRule(beforeAssign, "=", afterAssign));
    }

    private static Rule createValueRule() {
        final var value = new LazyRule();
        value.set(new OrRule(new JavaList<Rule>()
                .add(createTupleRule(value))
                .add(new TypeRule(INT_TYPE, new StripRule(new IntRule(INT_VALUE))))
        ));
        return value;
    }

    private static TypeRule createTupleRule(LazyRule value) {
        final var childRule = new SplitRule(new ValueSplitter(), TUPLE_VALUES, value);
        return new TypeRule(TUPLE_TYPE, new PrefixRule("[", new SuffixRule(childRule, "]")));
    }
}
