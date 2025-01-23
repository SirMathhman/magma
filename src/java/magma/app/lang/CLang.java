package magma.app.lang;

import magma.app.rule.LazyRule;
import magma.app.rule.OrRule;
import magma.app.rule.Rule;
import magma.app.rule.TypeRule;

import java.util.List;

public class CLang {
    public static Rule createCRootRule() {
        return new TypeRule(CommonLang.ROOT_TYPE, CommonLang.createContentRule(createCRootSegmentRule()));
    }

    private static OrRule createCRootSegmentRule() {
        final var function = new LazyRule();
        final var struct = new LazyRule();
        struct.set(JavaLang.createJavaCompoundRule(CommonLang.STRUCT_TYPE, "struct ", function, struct));

        return new OrRule(List.of(
                CommonLang.createNamespacedRule("import", "import "),
                struct,
                function,
                CommonLang.createWhitespaceRule()
        ));
    }
}
