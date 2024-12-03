package magma.app.compile.lang.c;

import magma.app.compile.rule.EmptyRule;
import magma.app.compile.rule.Rule;

public class CLang {
    public static Rule createRootRule() {
        return new EmptyRule();
    }
}
