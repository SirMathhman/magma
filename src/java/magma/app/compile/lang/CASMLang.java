package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class CASMLang {
    public static final String OP_CODE = "op-code";
    public static final String ADDRESS_OR_VALUE = "addressOrValue";
    public static final String CHAR_TYPE = "char";
    public static final String NUMBER_TYPE = "number";
    public static final String DATA_VALUE = "value";
    public static final String DATA_TYPE = "data";
    public static final String INSTRUCTION_TYPE = "instruction";
    public static final String CHILDREN = "children";
    public static final String SECTION_TYPE = "section";
    public static final String GROUP_NAME = "name";
    public static final String NUMBER_VALUE = "value";
    public static final String CHAR_VALUE = "value";
    public static final String DATA_NAME = "name";
    public static final String INSTRUCTION_LABEL = "label";
    public static final String LABEL_TYPE = "label";

    public static Rule createRootRule() {
        final var label = createGroupRule(LABEL_TYPE, "label ", createStatementRule());
        final var section = createGroupRule(SECTION_TYPE, "section ", new OrRule(List.of(
                new EmptyRule(),
                createDataRule(),
                label
        )));

        return new NodeListRule(CHILDREN, new StripRule(section));
    }

    private static Rule createGroupRule(String type, String prefix, Rule statement) {
        final var name = new StripRule(new StringRule(GROUP_NAME));
        final var children = new NodeListRule(CHILDREN, new StripRule(statement));
        return new TypeRule(type, new PrefixRule(prefix, new FirstRule(name, "{", new SuffixRule(children, "}"))));
    }

    private static Rule createStatementRule() {
        final var statement = new LazyRule();
        statement.setRule(new OrRule(List.of(
                new EmptyRule(),
                createInstructionRule()
        )));
        return statement;
    }

    private static TypeRule createInstructionRule() {
        final var opCode = new StringRule(OP_CODE);
        return new TypeRule(INSTRUCTION_TYPE, new SuffixRule(new OrRule(List.of(
                new FirstRule(opCode, " ", new StringRule(INSTRUCTION_LABEL)),
                opCode
        )), ";"));
    }

    private static Rule createDataRule() {
        final var name = new StripRule(new FilterRule(new SymbolFilter(), new StringRule(DATA_NAME)));
        final var value = new NodeRule(DATA_VALUE, createValueRule());

        return new TypeRule(DATA_TYPE, new FirstRule(name, "=", new StripRule(new SuffixRule(value, ";"))));
    }

    private static Rule createValueRule() {
        return new OrRule(List.of(
                createCharRule(),
                new TypeRule(NUMBER_TYPE, new StripRule(new FilterRule(new NumberFilter(), new StringRule(NUMBER_VALUE))))
        ));
    }

    private static TypeRule createCharRule() {
        return new TypeRule(CHAR_TYPE, new StripRule(new PrefixRule("'", new SuffixRule(new StringRule(CHAR_VALUE), "'"))));
    }
}
