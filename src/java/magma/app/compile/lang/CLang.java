package magma.app.compile.lang;

import magma.app.compile.rule.EmptyRule;
import magma.app.compile.rule.Rule;

public class CLang {
    public static Rule createCRootRule() {
        return new EmptyRule();
    }
}
