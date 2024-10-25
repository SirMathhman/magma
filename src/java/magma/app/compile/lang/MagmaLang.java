package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.createImportRule;

public class MagmaLang {
    public static final String TRAIT_TYPE = "trait";
    public static final String FUNCTION_TYPE = "function";

    public static Rule createRootRule() {
        return CommonLang.createRootRule(new OrRule(List.of(
                createImportRule(),
                new TypeRule(TRAIT_TYPE, new PrefixRule("trait ", new SuffixRule(new ExtractRule("name"), " {}"))),
                new TypeRule(FUNCTION_TYPE, new PrefixRule("class def ", new SuffixRule(new ExtractRule("name"), "() => {}"))),
                CommonLang.createWhitespaceRule()
        )));
    }
}
