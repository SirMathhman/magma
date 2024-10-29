package magma.compile.lang;

import magma.compile.rule.SuffixRule;

public class JavaLang {
    public static SuffixRule createPackageRule() {
        return CommonLang.createNamespaceRule("package");
    }
}
