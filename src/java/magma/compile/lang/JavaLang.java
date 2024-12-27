package magma.compile.lang;

import magma.NodeRule;
import magma.compile.rule.DiscardRule;
import magma.compile.rule.ExactRule;
import magma.compile.rule.LazyRule;
import magma.compile.rule.OrRule;
import magma.compile.rule.Rule;
import magma.compile.rule.TypeRule;
import magma.compile.rule.slice.NodeListRule;
import magma.compile.rule.slice.StatementSlicer;
import magma.compile.rule.split.LocatingSplitter;
import magma.compile.rule.split.SplitRule;
import magma.compile.rule.split.locate.FirstLocator;
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
        final var rightRule = CommonLang.createBlock(name, createClassMemberRule());
        return new TypeRule("class", new SplitRule(new DiscardRule(), new LocatingSplitter("class ", new FirstLocator()), rightRule));
    }

    private static Rule createClassMemberRule() {
        return new OrRule(List.of(
                createMethodRule(),
                CommonLang.createWhitespaceRule()
        ));
    }

    private static TypeRule createMethodRule() {
        final var definition = CommonLang.createDefinitionRule();
        final var wrapped = CommonLang.createBlock(definition, createStatementRule());
        return new TypeRule("method", wrapped);
    }

    private static Rule createStatementRule() {
        final var value = CommonLang.createValueRule();
        final LazyRule statement = new LazyRule();
        statement.set(new OrRule(List.of(
                createConditionedRule("if", "if ", value, statement),
                createConditionedRule("while", "while ", value, statement),
                new TypeRule("else", createElseRule(statement)),
                CommonLang.createInitializationRule(value),
                new SuffixRule(CommonLang.createInvocationRule(), ";")
        )));

        return statement;
    }

    private static OrRule createElseRule(LazyRule statement) {
        final var asBlock = CommonLang.createBlock(new ExactRule("else"), statement);
        return new OrRule(List.of(asBlock, new PrefixRule("else", new NodeRule("value", statement))));
    }

    private static TypeRule createConditionedRule(String type, String prefix, Rule value, LazyRule statement) {
        final var condition = new NodeRule("condition", value);
        final var anIf = new PrefixRule(prefix, new StripRule(new PrefixRule("(", new SuffixRule(condition, ")"))));
        return new TypeRule(type, CommonLang.createBlock(new StripRule(anIf), statement));
    }

    private static Rule createNamespacedRule(String type, String prefix) {
        final var namespace = new StringListRule("namespace", "\\.");
        return new TypeRule(type, new PrefixRule(prefix, new SuffixRule(namespace, ";")));
    }

}
