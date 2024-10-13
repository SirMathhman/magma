package magma;

import magma.rule.ExtractRule;
import magma.rule.PrefixRule;
import magma.rule.Rule;
import magma.rule.SuffixRule;

public class CommonLang {
    public static final String NAME = "name";
    public static final String STATEMENT_END = ";";
    public static final String NAMESPACE = "namespace";
    public static final Rule IMPORT_RULE = new PrefixRule("import ", new SuffixRule(STATEMENT_END, new ExtractRule(NAMESPACE)));
}
