package magma.app.compile.lang;

import magma.app.compile.rule.NodeListRule;
import magma.app.compile.rule.*;

import java.util.List;

public class MagmaLang {
    public static final String FUNCTION_PREFIX = "class def ";
    public static final String FUNCTION_SUFFIX = "() => {}";
    public static final String FUNCTION = "function";
    public static final Rule FUNCTION_RULE = new TypeRule(FUNCTION, new SuffixRule(new PrefixRule(FUNCTION_PREFIX, new ExtractRule(CommonLang.NAME)), FUNCTION_SUFFIX));
    public static final OrRule MAGMA_ROOT_MEMBER = new OrRule(List.of(CommonLang.IMPORT_RULE, FUNCTION_RULE));
    public static final NodeListRule MAGMA_ROOT_RULE = new NodeListRule(CommonLang.CHILDREN, MAGMA_ROOT_MEMBER);
}
