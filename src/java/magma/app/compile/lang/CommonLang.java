package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

public class CommonLang {
    public static final String NAME = "name";
    public static final String STATEMENT_END = ";";
    public static final String NAMESPACE = "namespace";

    public static final String IMPORT = "import";
    public static final String CHILDREN = "children";
    public static final String AFTER_IMPORT = "after-import";
    public static final String MODIFIER_VALUE = "modifier-value";
    public static final String MODIFIER_TYPE = "modifier";

    public static TypeRule createImportRule() {
        return createImportRule(IMPORT, "import ");
    }

    public static TypeRule createImportRule(String type, String prefix) {
        final var childRule = new SuffixRule(createNamespaceRule(), STATEMENT_END);
        return new TypeRule(type, new StripRule(new PrefixRule(prefix, childRule), "before-import", AFTER_IMPORT));
    }

    static Rule createNamespaceRule() {
        final var segment1 = new FilterRule(new SymbolFilter(), new ExtractRule("segment"));
        return new NodeListRule(new SimpleSplitter("."), NAMESPACE, new TypeRule("segment", new StripRule(segment1)));
    }

    static Rule createModifiersRule() {
        final var modifier = new TypeRule(MODIFIER_TYPE, new StripRule(new OrRule(List.of(new FilterRule(new ListFilter(JavaLang.MODIFIERS_LIST), new ExtractRule(MODIFIER_VALUE)), new EmptyRule()))));
        return new NodeListRule(new SimpleSplitter(" "), JavaLang.MODIFIERS, modifier);
    }

    static NodeListRule createChildrenRule(Rule statement) {
        return new NodeListRule(new StatementSplitter(), "children", new StripRule(statement, "", ""));
    }
}
