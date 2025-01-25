package magma.app.lang;

import magma.app.rule.ExactRule;
import magma.app.rule.LazyRule;
import magma.app.rule.OrRule;
import magma.app.rule.PrefixRule;
import magma.app.rule.Rule;
import magma.app.rule.StringRule;
import magma.app.rule.TypeRule;

import java.util.List;

import static magma.app.lang.CommonLang.ROOT_TYPE;
import static magma.app.lang.CommonLang.STRUCT_TYPE;
import static magma.app.lang.CommonLang.createContentRule;
import static magma.app.lang.CommonLang.createNamespacedRule;
import static magma.app.lang.CommonLang.createWhitespaceRule;
import static magma.app.lang.JavaLang.createJavaCompoundRule;

public class CLang {
    public static Rule createCRootRule() {
        return new TypeRule(ROOT_TYPE, createContentRule(createCRootSegmentRule()));
    }

    private static OrRule createCRootSegmentRule() {
        final var function = new LazyRule();
        final var struct = new LazyRule();
        struct.set(createJavaCompoundRule(STRUCT_TYPE, "struct ", function, struct));

        return new OrRule(List.of(
                createNamespacedRule("include", "#include \"", "/", ".h\""),
                new TypeRule("if-not-defined", new PrefixRule("#ifndef ", new StringRule("value"))),
                new TypeRule("define", new PrefixRule("#define ", new StringRule("value"))),
                new TypeRule("endif", new ExactRule("#endif")),
                struct,
                function,
                createWhitespaceRule()
        ));
    }
}
