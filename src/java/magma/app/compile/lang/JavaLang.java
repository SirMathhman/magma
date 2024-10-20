package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.*;

public class JavaLang {
    public static final String PACKAGE = "package";
    public static final Rule PACKAGE_RULE = new TypeRule(PACKAGE, new PrefixRule("package ", new SuffixRule(new ExtractRule(NAMESPACE), STATEMENT_END)));

    public static final String RECORD = "record";
    public static final Rule RECORD_RULE = new TypeRule(RECORD, new PrefixRule("record ", new SuffixRule(new ExtractRule(NAME), "(){}")));
    public static final OrRule JAVA_ROOT_MEMBER = new OrRule(List.of(
            PACKAGE_RULE,
            IMPORT_RULE,
            RECORD_RULE,
            createInterfaceRule()
    ));
    public static final NodeListRule JAVA_ROOT_RULE = new NodeListRule(new StatementSplitter(), CHILDREN, new StripRule(JAVA_ROOT_MEMBER));
    public static final String INTERFACE = "interface";
    public static final String MODIFIERS = "modifiers";
    public static final String METHOD = "method";

    private static TypeRule createInterfaceRule() {
        final var modifiers = new StringListRule(MODIFIERS, " ");
        final var memberRule = new OrRule(List.of(
                createMethodRule()
        ));

        final var content = new SuffixRule(new NodeListRule(new StatementSplitter(), "content", new StripRule(memberRule)), "}");
        final var name = new LocatingRule(new StripRule(new ExtractRule("name")), new FirstLocator("{"), new StripRule(content));
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
                new TypeRule("symbol", new StripRule(new ExtractRule("type")))
        )));
        return type;
    }

    private static TypeRule createGenericRule(LazyRule type) {
        final var base = new NodeRule("base", type);
        final var child = new NodeListRule(new ValueSplitter(), "children", type);

        return new TypeRule("generic", new StripRule(new LocatingRule(base, new FirstLocator("<"), new SuffixRule(child, ">"))));
    }
}
