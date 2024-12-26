package magma.compile.lang;

import magma.NodeRule;
import magma.compile.rule.DiscardRule;
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
import magma.compile.rule.string.StringListRule;
import magma.compile.rule.string.StringRule;
import magma.compile.rule.string.StripRule;
import magma.compile.rule.string.SuffixRule;
import magma.compile.rule.string.SymbolRule;

import java.util.List;

public class JavaLang {
    public static Rule createJavaRootRule() {
        return new TypeRule("group", new NodeListRule(new StatementSlicer(), "children", new OrRule(List.of(
                createNamespacedRule("package", "package "),
                createNamespacedRule("import", "import "),
                createClassRule(),
                CommonLang.createWhitespaceRule()
        ))));
    }

    private static TypeRule createClassRule() {
        final var name = new StripRule(new SymbolRule(new StringRule("name")));
        final var rightRule = CommonLang.wrapInBlock(name, createClassMemberRule());
        return new TypeRule("class", new SplitRule(new DiscardRule(), new LocatingSplitter("class ", new FirstLocator()), rightRule));
    }

    private static Rule createClassMemberRule() {
        return new OrRule(List.of(
                createMethodRule(),
                CommonLang.createWhitespaceRule()
        ));
    }

    private static TypeRule createMethodRule() {
        final var type = new NodeRule("type", CommonLang.createTypeRule());
        final var leftRule = new OrRule(List.of(
                new SplitRule(new DiscardRule(), new LocatingSplitter(" ", new BackwardsLocator()), type),
                type
        ));

        final var beforeParams = new SplitRule(leftRule, new LocatingSplitter(" ", new LastLocator()), new StringRule("name"));
        final var params = new NodeListRule(new TypeSlicer(), "params", new TypeRule("definition", beforeParams));
        final var withParams = new SplitRule(params, new LocatingSplitter(")", new FirstLocator()), new DiscardRule());
        return new TypeRule("method", new SplitRule(beforeParams, new LocatingSplitter("(", new FirstLocator()), withParams));
    }

    private static Rule createNamespacedRule(String type, String prefix) {
        final var namespace = new StringListRule("namespace", "\\.");
        return new TypeRule(type, new PrefixRule(prefix, new SuffixRule(namespace, ";")));
    }

}
