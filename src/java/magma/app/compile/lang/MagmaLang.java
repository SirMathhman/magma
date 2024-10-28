package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.*;

public class MagmaLang {
    public static final String TRAIT_TYPE = "trait";
    public static final String FUNCTION_TYPE = "function";
    public static final String STRUCT_TYPE = "struct";
    public static final List<String> MODIFIERS_LIST = List.of(
            "inline"
    );

    public static Rule createRootRule() {
        return createChildrenRule(new OrRule(List.of(
                createImportRule(),
                createFunctionRule(),
                createCompoundTypeRule(TRAIT_TYPE, "trait "),
                createCompoundTypeRule(STRUCT_TYPE, "struct "),
                new TypeRule("impl", new StripRule(new PrefixRule("impl", new ExtractRule("value")))),
                createWhitespaceRule()
        )));
    }

    private static TypeRule createFunctionRule() {
        final var afterKeyword = new EmptyRule();
        final var withModifiers = new LocatingRule(createModifiersRule(MODIFIERS_LIST), new FirstLocator(" def "), afterKeyword);
        return new TypeRule(FUNCTION_TYPE, new OptionalNodeListRule(MODIFIERS, withModifiers, new PrefixRule("def ", afterKeyword)));
    }

    private static TypeRule createCompoundTypeRule(String type, String prefix) {
        final var content = createChildrenRule(new OrRule(List.of(
                new StripRule(new SuffixRule(createDefinitionRule(), ";")),
                createWhitespaceRule()
        )));
        final var childRule = new LocatingRule(new StripRule(new ExtractRule("name")), new FirstLocator("{"), new SuffixRule(content, "}"));
        return new TypeRule(type, new StripRule(new PrefixRule(prefix, childRule)));
    }

    private static TypeRule createDefinitionRule() {
        return new TypeRule("definition", new LocatingRule(new StripRule(new ExtractRule("name")), new FirstLocator(":"), new ExtractRule("type")));
    }
}
