package magma.app.compile.lang;

import magma.app.compile.rule.*;

public class MagmaLang {

    public static final String RETURN_VALUE = "value";

    public static Rule createMagmaRootRule() {
        final var value = new StringRule(RETURN_VALUE);
        return new PrefixRule("return ", new SuffixRule(value, ";"));
    }
}
