package magma.app.compile.lang;

import magma.app.compile.rule.*;

public class CommonLang {
    public static final String ROOT_TYPE = "root";
    public static final String FUNCTION_TYPE = "function";
    public static final String BLOCK_CHILDREN = "children";

    static Rule createReturnRule() {
        final var value = new StringRule("value");
        return new TypeRule("return", new PrefixRule("return ", new SuffixRule(value, ";")));
    }
}
