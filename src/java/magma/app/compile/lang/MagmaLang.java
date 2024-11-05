package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class MagmaLang {
    public static Rule createMagmaStatementsRule() {
        final LazyRule statements = new LazyRule();
        statements.setRule(new NodeListRule(CommonLang.CHILDREN, new StripRule(new OrRule(List.of(
                createFunctionRule(statements),
                CommonLang.createDeclarationRule(createMagmaDefinitionRule()),
                CommonLang.createReturnRule()
        )))));
        return statements;
    }

    private static TypeRule createFunctionRule(LazyRule statements) {
        final var beforeContent = new StringRule("before-content");

        return new TypeRule("function", new PrefixRule("def ", new FirstRule(beforeContent, "{", new StripRule(new SuffixRule(statements, "}")))));
    }

    private static TypeRule createMagmaDefinitionRule() {
        final var name = new StripRule(new StringRule("name"));
        final var type = new NodeRule(CommonLang.DEFINITION_TYPE, createMagmaTypeRule());
        return new TypeRule("definition-type", new StripRule(new PrefixRule("let ", new FirstRule(name, ":", type))));
    }

    private static Rule createMagmaTypeRule() {
        return CommonLang.createSymbolTypeRule();
    }
}
