package magma;

public class MagmaLang {
    public static final String FUNCTION_PREFIX = "class def ";
    public static final String FUNCTION_SUFFIX = "() => {}";
    public static final String FUNCTION = "function";
    public static final Rule FUNCTION_RULE = new TypeRule(FUNCTION, new SuffixRule(FUNCTION_SUFFIX, new PrefixRule(FUNCTION_PREFIX, new ExtractRule(CommonLang.NAME))));
}
