package magma.app.compile.lang;

import magma.app.compile.rule.EmptyRule;
import magma.app.compile.rule.Rule;

public class MagmaLang {
    public static final String TRAIT_TYPE = "trait";
    public static final String FUNCTION_TYPE = "function";

    public static Rule createRootRule() {
        return new EmptyRule();
    }
}
