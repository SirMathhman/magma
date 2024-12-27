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
import magma.compile.rule.string.PrefixRule;
import magma.compile.rule.string.StringRule;
import magma.compile.rule.string.StripRule;
import magma.compile.rule.string.SuffixRule;
import magma.compile.rule.string.SymbolRule;

import java.util.List;

public class CommonLang {
    static Rule createGroupRule(Rule childRule) {
        final var children = new NodeListRule(new StatementSlicer(), "children", new StripRule("before-child", childRule, "after-child"));
        return new TypeRule("group", new StripRule("before-children", children, "after-children"));
    }

    static SplitRule createBlock(Rule beforeBlock, Rule blockMember) {
        final var value = new NodeRule("value", createGroupRule(blockMember));
        final var blockRule = new TypeRule("block", value);
        return new SplitRule(beforeBlock, new LocatingSplitter(" {", new FirstLocator()), new StripRule(new SuffixRule(new NodeRule("value", blockRule), "}")));
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
        return new TypeRule("symbol", new SymbolRule(new StringRule("value")));
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
                new SplitRule(new StringRule("before-type"), new LocatingSplitter(" ", new BackwardsLocator()), type),
                type
        ));

        final var name = new StripRule(new SymbolRule(new StringRule("name")));
        final var beforeParams = new SplitRule(leftRule, new LocatingSplitter(" ", new LastLocator()), name);

        final var params = new NodeListRule(new TypeSlicer(), "params", new TypeRule("definition", beforeParams));
        final var definition = new SplitRule(beforeParams, new LocatingSplitter("(", new FirstLocator()), new SuffixRule(params, ")"));
        return new OrRule(List.of(beforeParams, definition));
    }

    static TypeRule createInitializationRule(Rule valueRule) {
        final var definition = new NodeRule("definition", createDefinitionRule());
        final var value = new NodeRule("value", valueRule);
        return new TypeRule("initialization", new SplitRule(definition, new LocatingSplitter("=", new FirstLocator()), new SuffixRule(value, ";")));
    }

    static Rule createValueRule() {
        return new OrRule(List.of(
                createInvocationRule()
        ));
    }

    static TypeRule createInvocationRule() {
        return new TypeRule("invocation", new StringRule("content"));
    }

    static TypeRule createConditionedRule(String type, String prefix, Rule value, LazyRule statement) {
        final var condition = new NodeRule("condition", value);
        final var anIf = new PrefixRule(prefix, new StripRule(new PrefixRule("(", new SuffixRule(condition, ")"))));
        return new TypeRule(type, createBlock(new StripRule(anIf), statement));
    }

    static TypeRule createElseRule(LazyRule statement) {
        final var asBlock = createBlock(new ExactRule("else"), statement);
        return new TypeRule("else", new OrRule(List.of(asBlock, new PrefixRule("else", new NodeRule("value", statement)))));
    }
}
