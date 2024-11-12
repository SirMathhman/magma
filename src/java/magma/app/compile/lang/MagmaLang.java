package magma.app.compile.lang;

import magma.app.compile.rule.*;

public class MagmaLang {
    public static Rule createMagmaRootRule() {
        return new TypeRule("return", new PrefixRule("return", new SuffixRule(new EmptyRule(), ";")));
    }
}
