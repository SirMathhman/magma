package magma.app.lang;

import magma.app.rule.LazyRule;
import magma.app.rule.OrRule;
import magma.app.rule.Rule;
import magma.app.rule.TypeRule;

import java.util.List;

public class JavaLang {
    public static Rule createJavaRootRule() {
        return new TypeRule(CommonLang.ROOT_TYPE, CommonLang.createContentRule(createJavaRootSegmentRule()));
    }

    private static OrRule createJavaRootSegmentRule() {
        final var function = new LazyRule();
        final var struct = new LazyRule();
        struct.set(new OrRule(List.of(
                createJavaCompoundRule(CommonLang.CLASS_TYPE, "class ", function, struct),
                createJavaCompoundRule(CommonLang.RECORD_TYPE, "record ", function, struct),
                createJavaCompoundRule(CommonLang.INTERFACE_TYPE, "interface ", function, struct)
        )));

        return new OrRule(List.of(
                CommonLang.createNamespacedRule("package", "package "),
                CommonLang.createNamespacedRule("import", "import "),
                CommonLang.createWhitespaceRule(),
                struct
        ));
    }

    public static Rule createJavaCompoundRule(String type, String infix, LazyRule function, LazyRule struct) {
        final var statement = CommonLang.createStatementRule(function, struct);
        function.set(CommonLang.createMethodRule(statement));

        return CommonLang.createCompoundRule(type, infix, CommonLang.createStructSegmentRule(function, statement,
                struct));
    }
}
