package magma.compile.lang;

import magma.compile.rule.ExtractRule;
import magma.compile.rule.PrefixRule;
import magma.compile.rule.Rule;
import magma.compile.rule.SuffixRule;

public class CommonLang {

    public static final String NAMESPACE = "namespace";

    public static Rule createImportRule() {
        return createNamespaceRule("import ");
    }

    public static SuffixRule createNamespaceRule(String prefix) {
        final var namespace = new ExtractRule(NAMESPACE);
        return new SuffixRule(new PrefixRule(prefix, namespace), ";");
    }
}
