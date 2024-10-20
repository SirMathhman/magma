package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.*;

public class JavaLang {
    public static final String PACKAGE = "package";
    public static final Rule PACKAGE_RULE = new TypeRule(PACKAGE, new PrefixRule("package ", new SuffixRule(new ExtractRule(NAMESPACE), STATEMENT_END)));

    public static final String RECORD = "record";
    public static final Rule RECORD_RULE = new TypeRule(RECORD, new PrefixRule("record ", new SuffixRule(new ExtractRule(NAME), "(){}")));

    public static final OrRule JAVA_ROOT_MEMBER = new OrRule(List.of(
            PACKAGE_RULE,
            IMPORT_RULE,
            RECORD_RULE,
            createInterfaceRule()
    ));
    public static final NodeListRule JAVA_ROOT_RULE = new NodeListRule(CHILDREN, new StripRule(JAVA_ROOT_MEMBER));

    private static TypeRule createInterfaceRule() {
        final var modifiers = new ExtractRule("modifiers");
        final var memberRule = new OrRule(List.of(
                new ExtractRule("content")
        ));

        final var content = new SuffixRule(new NodeListRule("content", new StripRule(memberRule)), "}");
        return new TypeRule("interface", new FirstRule(modifiers, "interface", new FirstRule(new ExtractRule("name"), "{", new StripRule(content))));
    }
}
