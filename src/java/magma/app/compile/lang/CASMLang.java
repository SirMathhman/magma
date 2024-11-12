package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class CASMLang {
    public static final String OP_CODE = "op-code";
    public static final String MNEMONIC = "mnemonic";

    public static final String ADDRESS_OR_VALUE = "addressOrValue";
    public static final String CHAR_TYPE = "char";
    public static final String NUMBER_TYPE = "number";
    public static final String DATA_TYPE = "data";
    public static final String INSTRUCTION_TYPE = "instruction";
    public static final String CHILDREN = "children";
    public static final String SECTION_TYPE = "section";
    public static final String GROUP_NAME = "name";
    public static final String DATA_NAME = "name";
    public static final String INSTRUCTION_LABEL = "label";
    public static final String LABEL_TYPE = "label";
    public static final String GROUP_AFTER_NAME = "after-name";
    public static final String BLOCK_BEFORE_CHILD = "before-child";
    public static final String BLOCK_TYPE = "block";
    public static final String BLOCK_AFTER_CHILDREN = "after-children";
    public static final String GROUP_VALUE = "value";
    public static final String DATA_VALUE = "value";
    public static final String NUMBER_VALUE = "value";
    public static final String CHAR_VALUE = "value";

    public static Rule createRootRule() {
        final var label = createGroupRule(LABEL_TYPE, "label ", createStatementRule());
        final var section = createGroupRule(SECTION_TYPE, "section ", new OrRule(List.of(
                createDataRule(),
                label
        )));

        return new NodeListRule(CHILDREN, new StripRule(section));
    }

    private static Rule createGroupRule(String type, String prefix, Rule statement) {
        final var name = new StripRule("", new StringRule(GROUP_NAME), GROUP_AFTER_NAME);
        final var block = new NodeRule(GROUP_VALUE, createBlockRule(statement));
        return new TypeRule(type, new PrefixRule(prefix, new FirstRule(name, "{", new SuffixRule(block, "}"))));
    }

    private static TypeRule createBlockRule(Rule statement) {
        return new TypeRule(BLOCK_TYPE, new StripRule("", new NodeListRule(CHILDREN, new StripRule(BLOCK_BEFORE_CHILD, statement, "")), BLOCK_AFTER_CHILDREN));
    }

    private static Rule createStatementRule() {
        final var statement = new LazyRule();
        statement.setRule(new OrRule(List.of(
                createInstructionRule()
        )));
        return statement;
    }

    private static TypeRule createInstructionRule() {
        final var opCode = new StringRule(MNEMONIC);
        return new TypeRule(INSTRUCTION_TYPE, new SuffixRule(new OrRule(List.of(
                new FirstRule(opCode, " ", new OrRule(List.of(
                        new StringRule(INSTRUCTION_LABEL),
                        new IntRule(ADDRESS_OR_VALUE)
                ))),
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
