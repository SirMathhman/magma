package magma.app.compile.lang;

import magma.app.compile.rule.EmptyRule;
import magma.app.compile.rule.Rule;

public class MagmaLang {
    public static Rule createRootRule() {
        return CommonLang.createRootRule(new EmptyRule());
    }
}
