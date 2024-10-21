package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.*;

public class JavaLang {
    public static final String PACKAGE = "package";
    public static final String RECORD = "record";
    public static final String CLASS = "class";
    public static final String INTERFACE = "interface";
    public static final String MODIFIERS = "modifiers";
    public static final String METHOD = "method";

    public static TypeRule createPackageRule() {
        return new TypeRule(PACKAGE, new PrefixRule("package ", new SuffixRule(createNamespaceRule(), STATEMENT_END)));
    }

    public static NodeListRule createRootRule() {
        return new NodeListRule(new StatementSplitter(), CHILDREN, new StripRule(createRootMemberRule(), "", ""));
    }

    public static TypeRule createRecordRule() {
        return new TypeRule(RECORD, new LocatingRule(new ExtractRule("modifiers"), new FirstLocator("record "), new LocatingRule(new ExtractRule(NAME), new FirstLocator("("), new ExtractRule("params-and-body"))));
    }

    private static OrRule createRootMemberRule() {
        return new OrRule(List.of(
                createPackageRule(),
                createImportRule(),
                createRecordRule(),
                createInterfaceRule(),
                createClassRule()
        ));
    }

    private static TypeRule createClassRule() {
        final var afterKeyword = new LocatingRule(new ExtractRule("name"), new FirstLocator("{"), new ExtractRule("body"));
        return new TypeRule(CLASS, new LocatingRule(new ExtractRule("before-keyword"), new FirstLocator("class "), afterKeyword));
    }

    private static TypeRule createInterfaceRule() {
        final var modifiers = new StringListRule(MODIFIERS, " ");
        final var memberRule = new OrRule(List.of(
                createMethodRule()
        ));

        final var content = new SuffixRule(new NodeListRule(new StatementSplitter(), "content", new StripRule(memberRule, "", "")), "}");
        final var name = new LocatingRule(new StripRule(new ExtractRule("name"), "", ""), new FirstLocator("{"), new StripRule(content, "", ""));
        return new TypeRule(INTERFACE, new LocatingRule(modifiers, new FirstLocator("interface"), name));
    }

    private static TypeRule createMethodRule() {
        final var type = createTypeRule();
        final var name = new ExtractRule("name");
        final var returns = new NodeRule("returns", type);

        final var beforeParams = new LocatingRule(returns, new LastLocator(" "), name);
        final var params = new OptionalNodeRule("params", new NodeRule("params", new TypeRule("content", new ExtractRule("params"))), new EmptyRule());
        final var withParams = new SuffixRule(params, ");");

        return new TypeRule(METHOD, new LocatingRule(beforeParams, new FirstLocator("("), withParams));
    }

    private static Rule createTypeRule() {
        final var type = new LazyRule();
        type.setChildRule(new OrRule(List.of(
                createGenericRule(type),
                new TypeRule("symbol", new StripRule(new ExtractRule("type"), "", ""))
        )));
        return type;
    }

    private static TypeRule createGenericRule(LazyRule type) {
        final var base = new NodeRule("base", type);
        final var child = new NodeListRule(new ValueSplitter(), "children", type);

        return new TypeRule("generic", new StripRule(new LocatingRule(base, new FirstLocator("<"), new SuffixRule(child, ">")), "", ""));
    }
}
