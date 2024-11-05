package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class MagmaLang {
    public static Rule createMagmaRootRule() {
        return new NodeListRule(CommonLang.CHILDREN, new StripRule(new OrRule(List.of(
                CommonLang.createDeclarationRule(createMagmaDefinitionRule()),
                CommonLang.createReturnRule()
        ))));
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
