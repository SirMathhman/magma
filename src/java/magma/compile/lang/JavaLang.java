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
    public static final String ROOT_TYPE = "group";
    public static final String ROOT_CHILDREN = "children";
    public static final String PACKAGE_TYPE = "package";
    public static final String IMPORT_TYPE = "import";
    public static final String CLASS_TYPE = "class";
    public static final String METHOD_TYPE = "method";

    public static Rule createJavaRootRule() {
        return new TypeRule(ROOT_TYPE, new NodeListRule(new StatementSlicer(), ROOT_CHILDREN, new OrRule(List.of(
                createNamespacedRule(PACKAGE_TYPE, "package "),
                createNamespacedRule(IMPORT_TYPE, "import "),
                createClassRule(),
                CommonLang.createWhitespaceRule()
        ))));
    }

    private static TypeRule createClassRule() {
        final var name = new StripRule(new FilterRule(new SymbolFilter(), new StringRule("name")));
        final var rightRule = CommonLang.createBlockValueRule(CommonLang.LAMBDA_VALUE, name, createClassMemberRule());
        return new TypeRule(CLASS_TYPE, new SplitRule(new StringListRule(" ", "modifiers"), new LocatingSplitter("class ", new FirstLocator()), rightRule));
    }

    private static Rule createClassMemberRule() {
        return new OrRule(List.of(
                createMethodRule(CommonLang.createTypeRule()),
                CommonLang.createWhitespaceRule()
        ));
    }

    private static LazyRule createMethodRule(Rule typeRule) {
        final LazyRule method = new LazyRule();
        final var definition = CommonLang.createDefinitionRule(typeRule);
        final var wrapped = CommonLang.createBlockValueRule(CommonLang.LAMBDA_VALUE, definition, createStatementRule(typeRule, method));
        method.set(new TypeRule(METHOD_TYPE, wrapped));
        return method;
    }

    private static Rule createStatementRule(Rule typeRule, LazyRule function) {
        final LazyRule statement = new LazyRule();
        final var value = CommonLang.createValueRule(typeRule, statement, function);
        statement.set(new OrRule(List.of(
                CommonLang.createBlockStatementRule(statement),
                CommonLang.createReturnRule(value),
                CommonLang.createConditionedRule("if", "if ", value, statement),
                CommonLang.createConditionedRule("while", "while ", value, statement),
                CommonLang.createElseRule(statement),
                new SuffixRule(CommonLang.createInvocationStatementRule(value), ";"),
                new SuffixRule(CommonLang.createConstructionRule(value), ";"),
                CommonLang.createInitializationRule(value, typeRule),
                CommonLang.createAssignmentRule()
        )));

        return statement;
    }

    private static Rule createNamespacedRule(String type, String prefix) {
        final var namespace = new StringListRule("\\.", "namespace");
        return new TypeRule(type, new PrefixRule(prefix, new SuffixRule(namespace, ";")));
    }

}
