package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.*;

public class MagmaLang {
    public static final String TRAIT_TYPE = "trait";
    public static final String FUNCTION_TYPE = "function";

    public static Rule createRootRule() {
        return CommonLang.createRootRule(new OrRule(List.of(
                createImportRule(),
                createTraitRule(),
                new TypeRule(FUNCTION_TYPE, new PrefixRule("class def ", new SuffixRule(new ExtractRule("name"), "() => {}"))),
                CommonLang.createWhitespaceRule()
        )));
    }

    private static TypeRule createTraitRule() {
        final var name = new ExtractRule("name");
        final var withTypeParams = new LocatingRule(name, new FirstLocator("<"), new SuffixRule(createTypeParamsRule(createTypeRule()), ">"));
        final var withoutTypeParams = new SuffixRule(name, " ");
        final var maybeTypeParams = new OptionalNodeListRule(TYPE_PARAMS, withTypeParams, withoutTypeParams);
        return new TypeRule(TRAIT_TYPE, new PrefixRule("trait ", new SuffixRule(maybeTypeParams, "{}")));
    }

    private static TypeRule createTypeRule() {
        return createSymbolTypeRule();
    }
}
