package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class CLang {
    public static final String DEFINITION = "definition";
    public static final String FUNCTION_TYPE = "function";
    public static final String AFTER_STATEMENTS = "after-statements";
    public static final String BEFORE_STATEMENT = "before-statement";

    public static Rule createCRootRule() {
        return new NodeListRule(CommonLang.CHILDREN, new OrRule(List.of(
                createFunctionRule()
        )));
    }

    private static TypeRule createFunctionRule() {
        final var statement = new StripRule(BEFORE_STATEMENT, createCStatementRule(), "");
        final var statements = new StripRule("", new NodeListRule(CommonLang.CHILDREN, statement), AFTER_STATEMENTS);

        return new TypeRule(FUNCTION_TYPE, new PrefixRule("int main(){", new SuffixRule(statements, "}")));
    }

    private static Rule createCStatementRule() {
        return new OrRule(List.of(
                CommonLang.createDeclarationRule(createCDefinitionRule()),
                CommonLang.createReturnRule()
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
