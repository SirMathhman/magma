package magma.app.compile.lang;

import magma.app.compile.rule.EmptyRule;
import magma.app.compile.rule.OrRule;
import magma.app.compile.rule.Rule;

import java.util.List;

import static magma.app.compile.lang.CommonLang.createChildrenRule;

public class MagmaLang {
    public static final String TRAIT_TYPE = "trait";
    public static final String FUNCTION_TYPE = "function";

    public static Rule createRootRule() {
        return createChildrenRule(new OrRule(List.of(
                new EmptyRule()
        )));
    }
}
