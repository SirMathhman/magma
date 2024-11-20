package magma;

import magma.rule.*;

import java.util.List;

public class MagmaLang {
    public static final String FUNCTION_TYPE = "function";

    public static Rule createFunctionRule() {
        return new TypeRule(FUNCTION_TYPE, new PrefixRule("class def ", new SuffixRule(new StringRule(CommonLang.VALUE), "() => {}")));
    }

    static SplitRule createRootMagmaRule() {
        return new SplitRule(CommonLang.ROOT_CHILDREN, new OrRule(List.of(
                CommonLang.createInstanceImportRule(),
                createFunctionRule()
        )));
    }
}
