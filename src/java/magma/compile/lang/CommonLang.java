package magma.compile.lang;

import magma.NodeRule;
import magma.compile.rule.ExactRule;
import magma.compile.rule.LazyRule;
import magma.compile.rule.OrRule;
import magma.compile.rule.Rule;
import magma.compile.rule.TypeRule;
import magma.compile.rule.slice.NodeListRule;
import magma.compile.rule.slice.StatementSlicer;
import magma.compile.rule.slice.TypeSlicer;
import magma.compile.rule.split.LocatingSplitter;
import magma.compile.rule.split.SplitRule;
import magma.compile.rule.split.locate.BackwardsLocator;
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
import java.util.stream.Stream;

public class CommonLang {
    static Rule createGroupRule(Rule childRule) {
        final var children = new NodeListRule(new StatementSlicer(), "children", new StripRule("before-child", childRule, "after-child"));
        return new TypeRule("group", new StripRule("before-children", children, "after-children"));
    }

    static SplitRule createBlock(Rule beforeBlock, Rule blockMember) {
        final var value = new NodeRule("value", createGroupRule(blockMember));
        final var blockRule = new TypeRule("block", value);
        return new SplitRule(beforeBlock, new LocatingSplitter("{", new FirstLocator()), new StripRule(new SuffixRule(new NodeRule("value", blockRule), "}")));
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

    private static TypeRule createGenericRule(LazyRule type) {
        final var parent = new StringRule("parent");
        final var children = new NodeListRule(new TypeSlicer(), "children", type);
        return new TypeRule("generic", new SplitRule(parent, new LocatingSplitter("<", new FirstLocator()), new SuffixRule(children, ">")));
    }

    static TypeRule createWhitespaceRule() {
        return new TypeRule("whitespace", new StripRule(new ExactRule("")));
    }

    static Rule createDefinitionRule() {
        final var type = new NodeRule("type", createTypeRule());
        final var leftRule = new OrRule(List.of(
                new SplitRule(new StringListRule(" ", "modifiers"), new LocatingSplitter(" ", new BackwardsLocator()), type),
                type
        ));

        final var name = new StripRule(new FilterRule(new SymbolFilter(), new StringRule("name")));
        final var beforeParams = new StripRule(new SplitRule(leftRule, new LocatingSplitter(" ", new LastLocator()), name));

        final var params = new NodeListRule(new TypeSlicer(), "params", new TypeRule("definition", beforeParams));
        final var definition = new SplitRule(beforeParams, new LocatingSplitter("(", new FirstLocator()), new StripRule(new SuffixRule(params, ")")));
        return new TypeRule("definition", new OrRule(List.of(beforeParams, definition)));
    }

    static TypeRule createInitializationRule(Rule valueRule) {
        final var definition = new NodeRule("definition", createDefinitionRule());
        final var value = new NodeRule("value", valueRule);
        return new TypeRule("initialization", new SplitRule(definition, new LocatingSplitter("=", new FirstLocator()), new SuffixRule(value, ";")));
    }

    static Rule createValueRule() {
        final var value = new LazyRule();
        value.set(new OrRule(List.of(
                createInvocationRule(value),
                createNumberRule(),
                createAccessRule(value),
                createSymbolRule(),
                createOperatorRule(value),
                createStringRule()
        )));
        return value;
    }

    private static TypeRule createStringRule() {
        return new TypeRule("string", new StripRule(new PrefixRule("\"", new SuffixRule(new StringRule("string-value"), "\""))));
    }

    private static TypeRule createOperatorRule(LazyRule value) {
        return new TypeRule("less-than", new SplitRule(new NodeRule("left", value), new LocatingSplitter("<", new FirstLocator()), new NodeRule("right", value)));
    }

    private static TypeRule createAccessRule(LazyRule value) {
        final var object = new NodeRule("object", value);
        final var property = new StripRule(new FilterRule(new SymbolFilter(), new StringRule("property")));
        return new TypeRule("access", new SplitRule(object, new LocatingSplitter(".", new LastLocator()), property));
    }

    private static TypeRule createNumberRule() {
        return new TypeRule("number", new StripRule(new FilterRule(new NumberFilter(), new StringRule("number-value"))));
    }

    static TypeRule createInvocationRule(Rule value) {
        final var caller = new NodeRule("caller", value);
        final var arguments = new NodeListRule(new TypeSlicer(), "arguments", value);
        return new TypeRule("invocation", new StripRule(new SuffixRule(new SplitRule(caller, new LocatingSplitter("(", new InvocationLocator()), arguments), ")")));
    }

    static TypeRule createConditionedRule(String type, String prefix, Rule value, LazyRule statement) {
        final var condition = new NodeRule("condition", value);
        final var anIf = new PrefixRule(prefix, new StripRule(new PrefixRule("(", new SuffixRule(condition, ")"))));
        return new TypeRule(type, createBlock(new StripRule(anIf), statement));
    }

    static TypeRule createElseRule(LazyRule statement) {
        final var withBlock = createBlock(new ExactRule("else "), statement);
        final var withoutBlock = new PrefixRule("else ", new NodeRule("value", statement));
        return new TypeRule("else", new OrRule(List.of(withBlock, withoutBlock)));
    }

    static TypeRule createAssignmentRule() {
        return new TypeRule("assignment", new SplitRule(new StringRule("destination"), new LocatingSplitter("=", new FirstLocator()), new SuffixRule(new StringRule("source"), ";")));
    }

    static TypeRule createReturnRule() {
        return new TypeRule("return", new PrefixRule("return ", new SuffixRule(new StringRule("value"), ";")));
    }

    private static class InvocationLocator implements Locator {
        @Override
        public Stream<Integer> locate(String input, String infix) {
            var depth = 0;
            for (int i = input.length() - 1; i >= 0; i--) {
                final var c = input.charAt(i);
                if (c == '(' && depth == 0) {
                    return Stream.of(i);
                } else {
                    if (c == ')') depth++;
                    if (c == '(') depth--;
                }
            }

            return Stream.empty();
        }
    }
}
