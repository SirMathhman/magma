package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.*;

public class MagmaLang {
    public static final String TRAIT_TYPE = "trait";
    public static final String FUNCTION_TYPE = "function";

    public static Rule createRootRule() {
        return createChildrenRule(new OrRule(List.of(
                createImportRule(),
                createTraitRule(),
                new TypeRule(FUNCTION_TYPE, new EmptyRule()),
                createWhitespaceRule()
        )));
    }

    private static TypeRule createTraitRule() {
        final var content = createChildrenRule(new OrRule(List.of(
                new StripRule(new SuffixRule(createDefinitionRule(), ";")),
                createWhitespaceRule()
        )));
        final var childRule = new LocatingRule(new StripRule(new ExtractRule("name")), new FirstLocator("{"), new SuffixRule(content, "}"));
        return new TypeRule(TRAIT_TYPE, new StripRule(new PrefixRule("trait ", childRule)));
    }

    private static TypeRule createDefinitionRule() {
        return new TypeRule("definition", new LocatingRule(new StripRule(new ExtractRule("name")), new FirstLocator(":"), new ExtractRule("type")));
    }
}
