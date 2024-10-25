package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.MODIFIERS;
import static magma.app.compile.lang.CommonLang.createModifiersRule;

public class MagmaLang {
    public static final String FUNCTION = "function";
    public static final String TRAIT = "trait";
    public static final List<String> MAGMA_MODIFIERS = List.of("export", "class");

    public static Rule createRootRule() {
        return CommonLang.createChildrenRule(new OrRule(List.of(
                CommonLang.createImportRule(),
                new TypeRule("trait", new PrefixRule("trait", new EmptyRule())),
                createFunctionRule(),
                CommonLang.createWhitespaceRule()
        )));
    }

    private static TypeRule createFunctionRule() {
        final var afterKeyword = new SuffixRule(new ExtractRule("name"), "() => {}");

        final var withModifiers = new LocatingRule(createModifiersRule(MAGMA_MODIFIERS), new FirstLocator(" def "), afterKeyword);
        final var withoutModifiers = new PrefixRule("def ", afterKeyword);

        return new TypeRule("function", new OptionalNodeListRule(MODIFIERS, withModifiers, withoutModifiers));
    }
}
