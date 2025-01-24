package magma.app.lang;

import magma.app.rule.ExactRule;
import magma.app.rule.LazyRule;
import magma.app.rule.OrRule;
import magma.app.rule.PrefixRule;
import magma.app.rule.Rule;
import magma.app.rule.StringRule;
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
                CommonLang.createNamespacedRule("include", "#include \"", "/", ".h\""),
                new TypeRule("if-not-defined", new PrefixRule("#ifndef ", new StringRule("value"))),
                new TypeRule("define", new PrefixRule("#define ", new StringRule("value"))),
                new TypeRule("endif", new ExactRule("#endif")),
                struct,
                function,
                CommonLang.createWhitespaceRule()
        ));
    }
}
