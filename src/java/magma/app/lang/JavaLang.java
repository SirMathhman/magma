package magma.app.lang;

import magma.app.rule.LazyRule;
import magma.app.rule.OrRule;
import magma.app.rule.Rule;
import magma.app.rule.TypeRule;

import java.util.List;

import static magma.app.lang.CommonLang.CLASS_TYPE;
import static magma.app.lang.CommonLang.INTERFACE_TYPE;
import static magma.app.lang.CommonLang.RECORD_TYPE;
import static magma.app.lang.CommonLang.ROOT_TYPE;
import static magma.app.lang.CommonLang.createCompoundRule;
import static magma.app.lang.CommonLang.createContentRule;
import static magma.app.lang.CommonLang.createMethodRule;
import static magma.app.lang.CommonLang.createNamespacedRule;
import static magma.app.lang.CommonLang.createStatementRule;
import static magma.app.lang.CommonLang.createStructSegmentRule;
import static magma.app.lang.CommonLang.createWhitespaceRule;

public class JavaLang {
    public static Rule createJavaRootRule() {
        return new TypeRule(ROOT_TYPE, createContentRule(createJavaRootSegmentRule()));
    }

    private static OrRule createJavaRootSegmentRule() {
        final var function = new LazyRule();
        final var struct = new LazyRule();
        struct.set(new OrRule(List.of(
                createJavaCompoundRule(CLASS_TYPE, "class ", function, struct),
                createJavaCompoundRule(RECORD_TYPE, "record ", function, struct),
                createJavaCompoundRule(INTERFACE_TYPE, "interface ", function, struct)
        )));

        return new OrRule(List.of(
                createNamespacedRule("package", "package ", ".", ";"),
                createNamespacedRule("import", "import ", ".", ";"),
                createWhitespaceRule(),
                struct
        ));
    }

    public static Rule createJavaCompoundRule(
            String type,
            String infix,
            LazyRule function,
            LazyRule struct
    ) {
        final var statement = createStatementRule(function, struct);
        function.set(createMethodRule(statement));

        return createCompoundRule(type, infix, createStructSegmentRule(function, statement,
                struct));
    }
}
