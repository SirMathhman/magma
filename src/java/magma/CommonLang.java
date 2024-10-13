package magma;

import magma.rule.*;

public class CommonLang {
    public static final String NAME = "name";
    public static final String STATEMENT_END = ";";
    public static final String NAMESPACE = "namespace";

    public static final String IMPORT = "import";
    public static final Rule IMPORT_RULE = new TypeRule(IMPORT, new PrefixRule("import ", new SuffixRule(STATEMENT_END, new ExtractRule(NAMESPACE))));

    public static final String CHILDREN = "children";
}
