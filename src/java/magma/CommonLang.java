package magma;

import magma.rule.*;

public class CommonLang {
    public static final String IMPORT_KEYWORD_WITH_SPACE = "import ";
    public static final String STATEMENT_END = ";";
    public static final String IMPORT_TYPE = "import";
    public static final String ROOT_CHILDREN = "children";

    public static Rule createInstanceImportRule() {
        return createImportRule(IMPORT_TYPE, createNamespaceRule());
    }

    public static Rule createNamespaceRule() {
        return new SuffixRule(new StringRule(), STATEMENT_END);
    }

    public static Rule createImportRule(String type, Rule suffixRule) {
        return new TypeRule(type, new PrefixRule(IMPORT_KEYWORD_WITH_SPACE, suffixRule));
    }
}
