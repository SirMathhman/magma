package magma.compile.lang;

import magma.compile.rule.LazyRule;
import magma.compile.rule.NodeRule;
import magma.compile.rule.OrRule;
import magma.compile.rule.Rule;
import magma.compile.rule.TypeRule;
import magma.compile.rule.slice.NodeListRule;
import magma.compile.rule.slice.ValueSlicer;
import magma.compile.rule.split.LocatingSplitter;
import magma.compile.rule.split.SplitRule;
import magma.compile.rule.split.locate.FirstLocator;
import magma.compile.rule.string.PrefixRule;
import magma.compile.rule.string.StringListRule;
import magma.compile.rule.string.StringRule;
import magma.compile.rule.string.SuffixRule;

import java.util.List;

public class CLang {
    public static Rule createCRootRule() {
        return CommonLang.createGroupRule(createCRootMemberRule());
    }

    private static OrRule createCRootMemberRule() {
        return new OrRule(List.of(
                createIncludesRule(),
                createStructRule(),
                CommonLang.createWhitespaceRule()
        ));
    }

    private static Rule createStructRule() {
        final var name = new StringRule("name");
        final var wrapped = CommonLang.createBlockValueRule(name, createStructMemberRule(CommonLang.createTypeRule()));
        return new TypeRule("struct", new PrefixRule("struct ", wrapped));
    }

    private static Rule createStructMemberRule(Rule typeRule) {
        return new OrRule(List.of(
                createFunctionRule(typeRule),
                CommonLang.createWhitespaceRule()
        ));
    }

    private static TypeRule createFunctionRule(Rule typeRule) {
        final var type = new NodeRule("type", typeRule);
        final var name = new StringRule("name");
        final var params = new NodeListRule(new ValueSlicer(), "params", new SplitRule(type, new LocatingSplitter(" ", new FirstLocator()), name));
        final var rightRule = new SplitRule(name, new LocatingSplitter("(", new FirstLocator()), new SuffixRule(params, ")"));
        final var childRule = new SplitRule(type, new LocatingSplitter(" ", new FirstLocator()), rightRule);
        return new TypeRule("function", CommonLang.createBlockValueRule(childRule, createStatementRule(typeRule)));
    }

    private static Rule createStatementRule(Rule typeRule) {
        final var statement = new LazyRule();
        final var value = CommonLang.createValueRule(typeRule);
        statement.set(new OrRule(List.of(
                CommonLang.createBlockStatementRule(statement),
                new SuffixRule(CommonLang.createInvocationRule(value), ";"),
                new SuffixRule(CommonLang.createConstructionRule(value), ";"),
                CommonLang.createInitializationRule(value, typeRule),
                CommonLang.createConditionedRule("if", "if ", value, statement),
                CommonLang.createConditionedRule("while", "while ", value, statement),
                CommonLang.createElseRule(statement),
                CommonLang.createAssignmentRule(),
                CommonLang.createReturnRule(value)
        )));
        return statement;
    }

    private static Rule createIncludesRule() {
        final var namespace = new StringListRule("/", "namespace");
        return new TypeRule("include", new PrefixRule("#include \"", new SuffixRule(namespace, ".h\"")));
    }
}
