package magma.compile.lang;

import magma.compile.rule.ExactRule;
import magma.compile.rule.LazyRule;
import magma.compile.rule.NodeRule;
import magma.compile.rule.OrRule;
import magma.compile.rule.Rule;
import magma.compile.rule.TypeRule;
import magma.compile.rule.slice.NodeListRule;
import magma.compile.rule.slice.StatementSlicer;
import magma.compile.rule.slice.ValueSlicer;
import magma.compile.rule.split.LocatingSplitter;
import magma.compile.rule.split.SplitRule;
import magma.compile.rule.split.locate.FirstLocator;
import magma.compile.rule.split.locate.LastLocator;
import magma.compile.rule.split.locate.Locator;
import magma.compile.rule.string.FilterRule;
import magma.compile.rule.string.PrefixRule;
import magma.compile.rule.string.StringListRule;
import magma.compile.rule.string.StringRule;
import magma.compile.rule.string.StripRule;
import magma.compile.rule.string.SuffixRule;
import magma.compile.rule.string.filter.NumberFilter;
import magma.compile.rule.string.filter.SymbolFilter;

import java.util.List;
import java.util.Optional;

public class CommonLang {
    public static final String GROUP_TYPE = "group";
    public static final String GROUP_BEFORE_CHILD = "before-child";
    public static final String GROUP_CHILDREN = "children";
    public static final String GROUP_AFTER_CHILDREN = "after-children";

    static Rule createGroupRule(Rule childRule) {
        final var children = new NodeListRule(new StatementSlicer(), GROUP_CHILDREN, new StripRule(GROUP_BEFORE_CHILD, childRule, "after-child"));
        return new TypeRule(GROUP_TYPE, new StripRule("before-children", children, GROUP_AFTER_CHILDREN));
    }

    static SplitRule createBlockValueRule(Rule beforeBlock, Rule blockMember) {
        final var blockRule = createBlockTypeRule(blockMember);
        final var splitter = new LocatingSplitter("{", new FirstLocator());
        final var stripped = new StripRule(new SuffixRule(new NodeRule("value", blockRule), "}"));
        return new SplitRule(beforeBlock, splitter, stripped);
    }

    public static TypeRule createBlockTypeRule(Rule blockMember) {
        final var value = new NodeRule("value", createGroupRule(blockMember));
        return new TypeRule("block", value);
    }

    static Rule createTypeRule() {
        final LazyRule type = new LazyRule();
        type.set(new OrRule(List.of(
                new TypeRule("array", new SuffixRule(new NodeRule("child", type), "[]")),
                createGenericRule(type),
                createSymbolRule()
        )));
        return type;
    }

    private static TypeRule createSymbolRule() {
        return new TypeRule("symbol", new StripRule(new FilterRule(new SymbolFilter(), new StringRule("symbol-value"))));
    }

    private static TypeRule createGenericRule(Rule type) {
        final var parent = new StringRule("parent");
        final var children = new NodeListRule(new ValueSlicer(), GROUP_CHILDREN, type);
        return new TypeRule("generic", new SplitRule(parent, new LocatingSplitter("<", new FirstLocator()), new SuffixRule(children, ">")));
    }

    static TypeRule createWhitespaceRule() {
        return new TypeRule("whitespace", new StripRule(new ExactRule("")));
    }

    static Rule createDefinitionRule(Rule typeRule) {
        final var type = new NodeRule("type", typeRule);
        final var leftRule = new OrRule(List.of(
                new SplitRule(new StringListRule(" ", "modifiers"), new LocatingSplitter(" ", new TypeStartLocator()), type),
                type
        ));

        final var name = new StripRule(new FilterRule(new SymbolFilter(), new StringRule("name")));
        final var beforeParams = new StripRule(new SplitRule(leftRule, new LocatingSplitter(" ", new LastLocator()), name));

        final var params = new NodeListRule(new ValueSlicer(), "params", new TypeRule("definition", beforeParams));
        final var definition = new SplitRule(beforeParams, new LocatingSplitter("(", new FirstLocator()), new StripRule(new SuffixRule(params, ")")));
        return new TypeRule("definition", new OrRule(List.of(beforeParams, definition)));
    }

    static TypeRule createInitializationRule(Rule valueRule, Rule typeRule) {
        final var definition = new NodeRule("definition", createDefinitionRule(typeRule));
        final var value = new NodeRule("value", valueRule);
        return new TypeRule("initialization", new SplitRule(definition, new LocatingSplitter("=", new FirstLocator()), new SuffixRule(value, ";")));
    }

    static Rule createValueRule(Rule typeRule, LazyRule statement, LazyRule function) {
        final var value = new LazyRule();
        value.set(new OrRule(List.of(
                CommonLang.createBlockStatementRule(statement),
                createSymbolRule(),
                createStringRule(),
                createNumberRule(),
                createAccessRule("data-access", ".", value, typeRule),
                createAccessRule("function-access", "::", value, typeRule),
                createOperatorRule("less-than", "<", value),
                createOperatorRule("subtract", "-", value),
                createLambdaRule(value),
                createInvocationRule(value, "invocation-value"),
                createConstructionRule(value),
                function
        )));
        return value;
    }

    private static TypeRule createLambdaRule(Rule value) {
        return new TypeRule("lambda", new SplitRule(new OrRule(List.of(
                new NodeRule("param", createSymbolRule()),
                new StripRule(new ExactRule("()"))
        )), new LocatingSplitter("->", new FirstLocator()), new NodeRule("value", value)));
    }

    private static TypeRule createStringRule() {
        return new TypeRule("string", new StripRule(new PrefixRule("\"", new SuffixRule(new StringRule("string-value"), "\""))));
    }

    private static TypeRule createOperatorRule(String type, String infix, Rule value) {
        return new TypeRule(type, new SplitRule(new NodeRule("left", value), new LocatingSplitter(infix, new FirstLocator()), new NodeRule("right", value)));
    }

    private static TypeRule createAccessRule(String type, String infix, Rule value, Rule typeRule) {
        final var object = new NodeRule("object", value);
        final var property = new StripRule(new FilterRule(new SymbolFilter(), new StringRule("property")));
        final var maybeWithTypeArgument = new OrRule(List.of(
                new PrefixRule("<", new SplitRule(new NodeListRule(new ValueSlicer(), "type-arguments", typeRule), new LocatingSplitter(">", new LastLocator()), property)),
                property
        ));
        return new TypeRule(type, new SplitRule(object, new LocatingSplitter(infix, new LastLocator()), maybeWithTypeArgument, true));
    }

    private static TypeRule createNumberRule() {
        return new TypeRule("number", new StripRule(new FilterRule(new NumberFilter(), new StringRule("number-value"))));
    }

    static TypeRule createInvocationStatementRule(Rule value) {
        return createInvocationRule(value, "invocation-statement");
    }

    static TypeRule createInvocationRule(Rule value, String type) {
        return createArgumentRule(type, new NodeRule("caller", value), value);
    }

    static TypeRule createConstructionRule(Rule value) {
        final var caller1 = new NodeRule("caller", value);
        final var caller = new OrRule(List.of(
                caller1,
                new StripRule(new SuffixRule(caller1, "<>"))
        ));
        return createArgumentRule("construction", new StripRule(new PrefixRule("new ", caller)), value);
    }

    private static TypeRule createArgumentRule(String type, Rule caller, Rule value) {
        final var arguments = new NodeListRule(new ValueSlicer(), "arguments", value);
        return new TypeRule(type, new StripRule(new SuffixRule(new SplitRule(caller, new LocatingSplitter("(", new InvocationLocator()), arguments), ")")));
    }

    static TypeRule createConditionedRule(String type, String prefix, Rule value, Rule statement) {
        final var condition = new NodeRule("condition", value);
        final var anIf = new PrefixRule(prefix, new StripRule(new PrefixRule("(", new SuffixRule(condition, ")"))));
        return new TypeRule(type, createBlockValueRule(new StripRule(anIf), statement));
    }

    static TypeRule createElseRule(Rule statement) {
        final var withBlock = createBlockValueRule(new ExactRule("else "), statement);
        final var withoutBlock = new PrefixRule("else ", new NodeRule("value", statement));
        return new TypeRule("else", new OrRule(List.of(withBlock, withoutBlock)));
    }

    static TypeRule createAssignmentRule() {
        return new TypeRule("assignment", new SplitRule(new StringRule("destination"), new LocatingSplitter("=", new FirstLocator()), new SuffixRule(new StringRule("source"), ";")));
    }

    static TypeRule createReturnRule(Rule value) {
        final var value0 = new NodeRule("value", value);
        return new TypeRule("return", new PrefixRule("return ", new SuffixRule(value0, ";")));
    }

    static StripRule createBlockStatementRule(LazyRule statement) {
        return new StripRule(new PrefixRule("{", new SuffixRule(createBlockTypeRule(statement), "}")));
    }

    private static class InvocationLocator implements Locator {
        @Override
        public Optional<Integer> locate(String input, String infix) {
            var depth = 0;
            for (int i = input.length() - 1; i >= 0; i--) {
                final var c = input.charAt(i);
                if (c == '(' && depth == 0) {
                    return Optional.of(i);
                } else {
                    if (c == ')') depth++;
                    if (c == '(') depth--;
                }
            }

            return Optional.empty();
        }
    }

    private static class TypeStartLocator implements Locator {
        @Override
        public Optional<Integer> locate(String input, String infix) {
            var depth = 0;
            for (int i = input.length() - 1; i >= 0; i--) {
                var c = input.charAt(i);
                if (c == ' ' && depth == 0) {
                    return Optional.of(i);
                } else {
                    if (c == '>') depth++;
                    if (c == '<') depth--;
                }
            }

            return Optional.empty();
        }
    }
}
