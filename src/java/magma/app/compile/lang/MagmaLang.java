package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class MagmaLang {

    public static final String RETURN_VALUE = "value";
    public static final String DECLARATION_TYPE = "declaration";
    public static final String RETURN_TYPE = "return";
    public static final String ROOT_TYPE = "root";

    public static Rule createMagmaRootRule() {
        return new TypeRule(ROOT_TYPE, new NodeListRule("children", new StripRule(new OrRule(List.of(
                new TypeRule(DECLARATION_TYPE, new PrefixRule("let x = 200;", new EmptyRule())),
                createReturnRule()
        )))));
    }

    private static Rule createReturnRule() {
        final var value = new StringRule(RETURN_VALUE);
        return new TypeRule(RETURN_TYPE, new PrefixRule("return ", new SuffixRule(value, ";")));
    }
}
