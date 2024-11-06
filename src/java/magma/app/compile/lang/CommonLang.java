package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class CommonLang {
    public static final String ROOT_TYPE = "root";
    public static final String FUNCTION_TYPE = "function";
    public static final String BLOCK_CHILDREN = "children";
    public static final String DECLARATION_TYPE = "declaration";
    public static final String BLOCK_BEFORE_CHILD = "before-child";
    public static final String BLOCK_TYPE = "block";

    static Rule createReturnRule() {
        final var value = new StringRule("value");
        return new TypeRule("return", new StripRule(new PrefixRule("return ", new SuffixRule(value, ";"))));
    }

    static Rule createBlockRule(List<Rule> list) {
        return new TypeRule(BLOCK_TYPE, new NodeListRule(BLOCK_CHILDREN, new StripRule(BLOCK_BEFORE_CHILD, new OrRule(list), "after-child")));
    }
}
