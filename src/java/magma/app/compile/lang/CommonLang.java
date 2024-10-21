package magma.app.compile.lang;

import magma.app.compile.rule.*;

public class CommonLang {
    public static final String NAME = "name";
    public static final String STATEMENT_END = ";";
    public static final String NAMESPACE = "namespace";

    public static final String IMPORT = "import";
    public static final String CHILDREN = "children";

    public static TypeRule createImportRule() {
        return new TypeRule(IMPORT, new PrefixRule("import ", new SuffixRule(createNamespaceRule(), STATEMENT_END)));
    }

    static Rule createNamespaceRule() {
        return new StringListRule(NAMESPACE, "\\.");
    }
}
