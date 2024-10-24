package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.createModifiersRule;

public class MagmaLang {
    public static final String FUNCTION_PREFIX = "class def ";
    public static final String FUNCTION_SUFFIX = "() => {}";
    public static final String FUNCTION = "function";
    public static final String TRAIT = "trait";

    public static Rule createFunctionRule() {
        final var name = CommonLang.NAME;

        final var withModifiers = new LocatingRule(createModifiersRule(), new FirstLocator(" " + FUNCTION_PREFIX), new ExtractRule(name));
        final var childRule = new OrRule(List.of(withModifiers, new PrefixRule(FUNCTION_PREFIX, new ExtractRule(name))));

        return new TypeRule(FUNCTION, new SuffixRule(childRule, FUNCTION_SUFFIX));
    }

    private static OrRule createRootMemberRule() {
        return new OrRule(List.of(
                CommonLang.createImportRule(),
                createFunctionRule(),
                new TypeRule(TRAIT, new EmptyRule())
        ));
    }

    public static Rule createRootRule() {
        return new NodeListRule(new StatementSplitter(), CommonLang.CHILDREN, createRootMemberRule());
    }
}
