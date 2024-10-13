package magma;

import magma.rule.*;

import java.util.List;

public class JavaLang {
    public static final String PACKAGE = "package";
    public static final Rule PACKAGE_RULE = new TypeRule(PACKAGE, new PrefixRule("package ", new SuffixRule(CommonLang.STATEMENT_END, new ExtractRule(CommonLang.NAMESPACE))));

    public static final Rule RECORD_RULE = new PrefixRule("record ", new SuffixRule("(){}", new ExtractRule(CommonLang.NAME)));
    public static final OrRule JAVA_ROOT_MEMBER = new OrRule(List.of(
            PACKAGE_RULE,
            CommonLang.IMPORT_RULE,
            RECORD_RULE
    ));
}
