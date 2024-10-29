package magma.compile;

import magma.compile.rule.ExtractRule;
import magma.compile.rule.PrefixRule;
import magma.compile.rule.Rule;
import magma.compile.rule.SuffixRule;

public class CommonLang {
    public static Rule createImportRule() {
        final var namespace = new ExtractRule("namespace");
        return new SuffixRule(new PrefixRule("import ", namespace), ";");
    }
}
