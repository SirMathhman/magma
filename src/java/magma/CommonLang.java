package magma;

public class CommonLang {
    public static final String NAME = "name";
    public static final String STATEMENT_END = ";";
    public static final String NAMESPACE = "namespace";
    public static final Rule IMPORT = new PrefixRule("import ", new SuffixRule(STATEMENT_END, new ExtractRule(NAMESPACE)));
}
