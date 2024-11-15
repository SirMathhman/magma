package magma.app.compile.lang;

import magma.app.compile.rule.*;
import magma.java.JavaList;

import java.util.List;

public class CASMLang {
    public static final String OP_CODE = "op-code";
    public static final String INSTRUCTION_MNEMONIC = "mnemonic";

    public static final String INSTRUCTION_ADDRESS_OR_VALUE = "addressOrValue";
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
    public static final String ROOT_TYPE = "root";
    public static final String ROOT_AFTER_SECTION = "";
    public static final String GROUP_AFTER = "after";
    public static final String DATA_AFTER_NAME = "after-name";
    public static final String DATA_BEFORE_VALUE = "before-value";
    public static final String COMMENT_TYPE = "comment";
    public static final String COMMENT_VALUE = "value";

    public static Rule createRootRule() {
        final var label = createGroupRule(LABEL_TYPE, "label ", createStatementRule());
        final var section = createGroupRule(SECTION_TYPE, "section ", new OrRule(new JavaList<>(List.of(
                createDataRule(),
                label
        ))));

        final List<Rule> section1 = List.of(
                section,
                new StripRule(new EmptyRule())
        );
        return new TypeRule(ROOT_TYPE, new NodeListRule(new BracketSplitter(), CHILDREN, new StripRule(new OrRule(new JavaList<>(section1)))));
    }

    private static Rule createGroupRule(String type, String prefix, Rule statement) {
        final var name = new StripRule("", new StringRule(GROUP_NAME), GROUP_AFTER_NAME);
        final var block = new NodeRule(GROUP_VALUE, createBlockRule(statement));
        final var childRule = new PrefixRule(prefix, new FirstRule(new FirstLocator("{"), name, "{", new SuffixRule(block, "}")));
        return new TypeRule(type, new StripRule("", childRule, GROUP_AFTER));
    }

    private static TypeRule createBlockRule(Rule statement) {
        return new TypeRule(BLOCK_TYPE, new StripRule("", new NodeListRule(new BracketSplitter(), CHILDREN, new StripRule(BLOCK_BEFORE_CHILD, statement, "")), BLOCK_AFTER_CHILDREN));
    }

    private static Rule createStatementRule() {
        final var statement = new LazyRule();
        statement.setRule(new OrRule(new JavaList<>(List.of(
                createCommentRule(),
                createInstructionRule()
        ))));
        return statement;
    }

    private static TypeRule createCommentRule() {
        final var value = new StringRule(COMMENT_VALUE);
        return new TypeRule(COMMENT_TYPE, new StripRule(new PrefixRule("// ", new SuffixRule(value, ";"))));
    }

    private static TypeRule createInstructionRule() {
        final var mnemonic = new StringRule(INSTRUCTION_MNEMONIC);

        final var withValue = new FirstRule(new FirstLocator(" "), mnemonic, " ", new OrRule(new JavaList<Rule>()
                .addLast(new StringRule(INSTRUCTION_LABEL))
                .addLast(new IntRule(INSTRUCTION_ADDRESS_OR_VALUE))));

        final var instructionOptions = new JavaList<Rule>()
                .addLast(withValue)
                .addLast(mnemonic);

        return new TypeRule(INSTRUCTION_TYPE, new SuffixRule(new OrRule(instructionOptions), ";"));
    }

    private static Rule createDataRule() {
        final var nameFilter = new FilterRule(new SymbolFilter(), new StringRule(DATA_NAME));
        final var name = new StripRule("", nameFilter, DATA_AFTER_NAME);
        final var value = new StripRule(DATA_BEFORE_VALUE, new NodeRule(DATA_VALUE, createValueRule()), "");

        return new TypeRule(DATA_TYPE, new FirstRule(new FirstLocator("="), name, "=", new StripRule(new SuffixRule(value, ";"))));
    }

    private static Rule createValueRule() {
        final List<Rule> charRule = List.of(
                createCharRule(),
                new TypeRule(NUMBER_TYPE, new StripRule(new FilterRule(new NumberFilter(), new StringRule(NUMBER_VALUE))))
        );
        return new OrRule(new JavaList<>(charRule));
    }

    private static TypeRule createCharRule() {
        return new TypeRule(CHAR_TYPE, new StripRule(new PrefixRule("'", new SuffixRule(new StringRule(CHAR_VALUE), "'"))));
    }
}
