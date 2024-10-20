package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.*;

public class JavaLang {
    public static final String PACKAGE = "package";
    public static final Rule PACKAGE_RULE = new TypeRule(PACKAGE, new PrefixRule("package ", new SuffixRule(STATEMENT_END, new ExtractRule(NAMESPACE))));

    public static final String RECORD = "record";
    public static final Rule RECORD_RULE = new TypeRule(RECORD, new PrefixRule("record ", new SuffixRule("(){}", new ExtractRule(NAME))));

    public static final OrRule JAVA_ROOT_MEMBER = new OrRule(List.of(
            PACKAGE_RULE,
            IMPORT_RULE,
            RECORD_RULE,
            new TypeRule("interface", new FirstRule(new ExtractRule("modifiers"), "interface", new ExtractRule("content")))
    ));

    public static final NodeListRule JAVA_ROOT_RULE = new NodeListRule(CHILDREN, new StripRule(JAVA_ROOT_MEMBER));
}
