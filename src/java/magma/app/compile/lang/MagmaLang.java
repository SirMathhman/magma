package magma.app.compile.lang;

import magma.app.compile.rule.EmptyRule;
import magma.app.compile.rule.Rule;

public class MagmaLang {
    public static final String FUNCTION = "function";
    public static final String TRAIT = "trait";

    public static Rule createRootRule() {
        return new EmptyRule();
    }
}
