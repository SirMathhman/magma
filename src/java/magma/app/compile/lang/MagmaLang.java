package magma.app.compile.lang;

import magma.app.compile.rule.*;

public class MagmaLang {
    public static Rule createMagmaRootRule() {
        return new EmptyRule();
    }
}
