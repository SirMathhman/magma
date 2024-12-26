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
import magma.compile.rule.split.locate.FirstLocator;
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
}
