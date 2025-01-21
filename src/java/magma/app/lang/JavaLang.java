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
        return new OrRule(List.of(
                CommonLang.createNamespacedRule("package", "package "),
                CommonLang.createNamespacedRule("import", "import "),
                createJavaCompoundRule(CommonLang.CLASS_TYPE, "class ", function),
                createJavaCompoundRule(CommonLang.RECORD_TYPE, "record ", function),
                createJavaCompoundRule(CommonLang.INTERFACE_TYPE, "interface ", function),
                CommonLang.createWhitespaceRule()
        ));
    }

    public static Rule createJavaCompoundRule(String type, String infix, LazyRule function) {
        final var statement = CommonLang.createStatementRule(function);
        function.set(CommonLang.createMethodRule(statement));

        return CommonLang.createCompoundRule(type, infix, CommonLang.createStructSegmentRule(function, statement));
    }
}
