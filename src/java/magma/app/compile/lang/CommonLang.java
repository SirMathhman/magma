package magma.app.compile.lang;

import magma.app.compile.rule.*;

public class CommonLang {
    public static final String NAME = "name";
    public static final String STATEMENT_END = ";";
    public static final String NAMESPACE = "namespace";

    public static final String IMPORT = "import";
    public static final String CHILDREN = "children";
    public static final String AFTER_IMPORT = "after-import";

    public static TypeRule createImportRule() {
        final var childRule = new SuffixRule(createNamespaceRule(), STATEMENT_END);
        return new TypeRule(IMPORT, new StripRule(new PrefixRule("import ", childRule), "before-import", AFTER_IMPORT));
    }

    static Rule createNamespaceRule() {
        return new StringListRule(NAMESPACE, "\\.");
    }
}
