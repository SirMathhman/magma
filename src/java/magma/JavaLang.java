package magma;

public class JavaLang {
    public static final String PACKAGE = "package";
    public static final Rule PACKAGE_RULE = new TypeRule(PACKAGE, new PrefixRule("package ", new SuffixRule(CommonLang.STATEMENT_END, new ExtractRule(CommonLang.NAMESPACE))));

    public static final Rule RECORD = new PrefixRule("record ", new SuffixRule("(){}", new ExtractRule(CommonLang.NAME)));
}
