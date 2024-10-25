package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class MagmaLang {
    public static final String FUNCTION = "function";
    public static final String TRAIT = "trait";

    public static Rule createRootRule() {
        return CommonLang.createChildrenRule(new OrRule(List.of(
                CommonLang.createImportRule(),
                new TypeRule("trait", new PrefixRule("trait", new EmptyRule())),
                new TypeRule("function", new PrefixRule("function", new EmptyRule())),
                CommonLang.createWhitespaceRule()
        )));
    }
}
