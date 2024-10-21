package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class MagmaLang {
    public static final String FUNCTION_PREFIX = "class def ";
    public static final String FUNCTION_SUFFIX = "() => {}";
    public static final String FUNCTION = "function";
    public static final String TRAIT = "trait";

    public static Rule createFunctionRule() {
        return new TypeRule(FUNCTION, new SuffixRule(new PrefixRule(FUNCTION_PREFIX, new ExtractRule(CommonLang.NAME)), FUNCTION_SUFFIX));
    }

    private static OrRule createRootMemberRule() {
        return new OrRule(List.of(
                CommonLang.IMPORT_RULE,
                createFunctionRule(),
                new TypeRule(TRAIT, new EmptyRule())
        ));
    }

    public static Rule createRootRule() {
        return new NodeListRule(new StatementSplitter(), CommonLang.CHILDREN, createRootMemberRule());
    }
}
