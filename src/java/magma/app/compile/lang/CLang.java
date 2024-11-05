package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class CLang {
    public static final String DEFINITION = "definition";
    public static final String AFTER_STATEMENTS = "after-statements";
    public static final String BEFORE_STATEMENT = "before-statement";

    public static Rule createCRootRule() {
        return new NodeListRule(CommonLang.CHILDREN, new OrRule(List.of(
                createFunctionRule()
        )));
    }

    private static Rule createFunctionRule() {
        final LazyRule functionRule = new LazyRule();
        final var statement = new StripRule(BEFORE_STATEMENT, createCStatementRule(functionRule), "");
        final var statements = new StripRule("", new NodeListRule(CommonLang.CHILDREN, statement), AFTER_STATEMENTS);

        final var childRule = new SuffixRule(statements, "}");
        final var header = new PrefixRule("int ", new SuffixRule(new StringRule("name"), "()"));
        functionRule.setRule(new TypeRule(CommonLang.FUNCTION_TYPE, new FirstRule(header, "{", childRule)));
        return functionRule;
    }

    private static Rule createCStatementRule(LazyRule functionRule) {
        return new OrRule(List.of(
                CommonLang.createDeclarationRule(createCDefinitionRule()),
                CommonLang.createReturnRule(),
                functionRule
        ));
    }

    private static TypeRule createCDefinitionRule() {
        final var name = new StringRule("name");
        final var type = new NodeRule(CommonLang.DEFINITION_TYPE, createCTypeRule());
        return new TypeRule("definition-type", new FirstRule(type, " ", name));
    }

    private static Rule createCTypeRule() {
        return CommonLang.createSymbolTypeRule();
    }

}
