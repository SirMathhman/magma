package magma.compile.lang;

import magma.compile.rule.LazyRule;
import magma.compile.rule.OrRule;
import magma.compile.rule.Rule;
import magma.compile.rule.TypeRule;
import magma.compile.rule.slice.NodeListRule;
import magma.compile.rule.slice.StatementSlicer;
import magma.compile.rule.split.LocatingSplitter;
import magma.compile.rule.split.SplitRule;
import magma.compile.rule.split.locate.FirstLocator;
import magma.compile.rule.string.FilterRule;
import magma.compile.rule.string.PrefixRule;
import magma.compile.rule.string.StringListRule;
import magma.compile.rule.string.StringRule;
import magma.compile.rule.string.StripRule;
import magma.compile.rule.string.SuffixRule;
import magma.compile.rule.string.filter.SymbolFilter;

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
        final var name = new StripRule(new FilterRule(new SymbolFilter(), new StringRule("name")));
        final var rightRule = CommonLang.createBlock(name, createClassMemberRule());
        return new TypeRule("class", new SplitRule(new StringListRule(" ", "modifiers"), new LocatingSplitter("class ", new FirstLocator()), rightRule));
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
        final LazyRule statement = new LazyRule();
        final var value = CommonLang.createValueRule(statement);
        statement.set(new OrRule(List.of(
                CommonLang.createConditionedRule("if", "if ", value, statement),
                CommonLang.createConditionedRule("while", "while ", value, statement),
                CommonLang.createElseRule(statement),
                CommonLang.createInitializationRule(value),
                new SuffixRule(CommonLang.createInvocationRule(value), ";"),
                CommonLang.createAssignmentRule(),
                CommonLang.createReturnRule()
        )));

        return statement;
    }

    private static Rule createNamespacedRule(String type, String prefix) {
        final var namespace = new StringListRule("\\.", "namespace");
        return new TypeRule(type, new PrefixRule(prefix, new SuffixRule(namespace, ";")));
    }

}
