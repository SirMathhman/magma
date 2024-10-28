package magma.app.compile.lang;

import magma.app.compile.rule.EmptyRule;
import magma.app.compile.rule.OrRule;
import magma.app.compile.rule.Rule;
import magma.app.compile.rule.TypeRule;

import java.util.List;

import static magma.app.compile.lang.CommonLang.*;

public class MagmaLang {
    public static final String TRAIT_TYPE = "trait";
    public static final String FUNCTION_TYPE = "function";

    public static Rule createRootRule() {
        return createChildrenRule(new OrRule(List.of(
                createImportRule(),
                new TypeRule(TRAIT_TYPE, new EmptyRule()),
                new TypeRule(FUNCTION_TYPE, new EmptyRule()),
                createWhitespaceRule()
        )));
    }
}
