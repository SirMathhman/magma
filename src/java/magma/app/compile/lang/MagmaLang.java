package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.createChildrenRule;
import static magma.app.compile.lang.CommonLang.createModifiersRule;

public class MagmaLang {
    public static final String FUNCTION_PREFIX = "class def ";
    public static final String FUNCTION = "function";
    public static final String TRAIT = "trait";

    public static Rule createFunctionRule() {
        final var name = CommonLang.NAME;

        final var withModifiers = new LocatingRule(createModifiersRule(), new FirstLocator(" " + FUNCTION_PREFIX), new ExtractRule(name));
        final var childRule = new OrRule(List.of(withModifiers, new PrefixRule(FUNCTION_PREFIX, new ExtractRule(name))));

        final var children = new OptionalNodeListRule("children", new EmptyRule(), new EmptyRule());
        return new TypeRule(FUNCTION, new LocatingRule(childRule, new FirstLocator("() => {"), new SuffixRule(children, "}")));
    }

    private static Rule createStatementRule() {
        return new OrRule(List.of(new EmptyRule()));
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
