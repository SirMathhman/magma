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
    public static final List<String> MODIFIERS_LIST = List.of(
            "public",
            "private",
            "static"
    );

    public static TypeRule createPackageRule() {
        return new TypeRule(PACKAGE, new PrefixRule("package ", new SuffixRule(createNamespaceRule(), STATEMENT_END)));
    }

    public static NodeListRule createRootRule() {
        return new NodeListRule(new StatementSplitter(), CHILDREN, new StripRule(createRootMemberRule(), "", ""));
    }

    public static TypeRule createRecordRule() {
        final var simpleName = new ExtractRule(NAME);
        final var typeParams = new NodeListRule(new ValueSplitter(), "type-params", createTypeRule());
        final var name = new OptionalNodeRule("type-params", new LocatingRule(simpleName, new FirstLocator("<"), new SuffixRule(typeParams, ">")), simpleName);

        final var params = new NodeListRule(new ValueSplitter(), "params", createDefinitionRule());
        final var withChildren = new NodeListRule(new StatementSplitter(), "children", new StripRule(createClassMemberRule(), "", ""));
        final var maybeChildren = new StripRule(new OptionalNodeRule("children", withChildren, new EmptyRule()), "", "");

        final var anInterface = new NodeRule("interface", createTypeRule());
        final var implementsPresent = new StripRule(new PrefixRule("implements", new LocatingRule(anInterface, new FirstLocator("{"), new SuffixRule(maybeChildren, "}"))), "", "");
        final var implementsEmpty = new StripRule(new PrefixRule("{", new SuffixRule(maybeChildren, "}")), "", "");
        final var afterParams = new OptionalNodeRule("super", implementsPresent, implementsEmpty);
        final var afterKeyword = new LocatingRule(name, new FirstLocator("("), new LocatingRule(params, new FirstLocator(")"), afterParams));
        return new TypeRule(RECORD, new LocatingRule(createModifiersRule(), new FirstLocator("record "), afterKeyword));
    }

    private static TypeRule createDefinitionRule() {
        final var content = new NodeRule("returns", createTypeRule());
        final var typeParams = new StripRule(new PrefixRule("<", new SuffixRule(new ExtractRule("type-params"), ">")), "", "");
        final var withTypeParams = new ContextRule("With type params.", new LocatingRule(typeParams, new ForwardsLocator(" "), content));
        final var maybeTypeParams = new OptionalNodeRule("type-params", withTypeParams, content);

        final var withModifiers = new ContextRule("With modifiers.", new LocatingRule(createModifiersRule(), new ForwardsLocator(" "), maybeTypeParams));
        final var maybeModifiers = new StripRule(new OptionalNodeRule("modifiers", withModifiers, maybeTypeParams), "", "");

        final var annotation = new TypeRule("annotation", new StripRule(new PrefixRule("@", new ExtractRule("value")), "", ""));
        final var annotations = new NodeListRule(new SimpleSplitter("\n"), "annotations", annotation);
        final var withAnnotations = new ContextRule("With annotations.", new LocatingRule(annotations, new LastLocator("\n"), maybeModifiers));
        final var maybeAnnotations = new OptionalNodeRule("annotations", withAnnotations, maybeModifiers);

        return new TypeRule("definition", new LocatingRule(maybeAnnotations, new LastLocator(" "), new ExtractRule("name")));
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
        final var memberRule = createClassMemberRule();
        final var content = new SuffixRule(new NodeListRule(new StatementSplitter(), "content", new StripRule(memberRule, "", "")), "}");
        final var name = new LocatingRule(new StripRule(new ExtractRule("name"), "", ""), new FirstLocator("{"), new StripRule(content, "", ""));
        return new TypeRule(INTERFACE, new LocatingRule(createModifiersRule(), new FirstLocator("interface"), name));
    }

    private static OrRule createClassMemberRule() {
        return new OrRule(List.of(
                createMethodRule()
        ));
    }

    private static Rule createModifiersRule() {
        final var modifier = new TypeRule("modifier", new FilterRule(new ListFilter(MODIFIERS_LIST), new ExtractRule("content")));
        return new NodeListRule(new SimpleSplitter(" "), MODIFIERS, modifier);
    }

    private static TypeRule createMethodRule() {
        final var params = new OptionalNodeRule("params", new NodeListRule(new ValueSplitter(), "params", createDefinitionRule()), new EmptyRule());

        final var children = new StripRule(new PrefixRule("{", new SuffixRule(new NodeListRule(new StatementSplitter(), "children", new StripRule(createStatementRule(), "", "")), "}")), "", "");
        final var maybeChildren = new OptionalNodeRule("children", children, new SuffixRule(new EmptyRule(), ";"));
        final var withParams = new LocatingRule(params, new FirstLocator(")"), maybeChildren);

        return new TypeRule(METHOD, new LocatingRule(createDefinitionRule(), new FirstLocator("("), withParams));
    }

    private static Rule createStatementRule() {
        return new OrRule(List.of(
                new TypeRule("return", new PrefixRule("return ", new SuffixRule(new NodeRule("value", createValueRule()), ";")))
        ));
    }

    private static Rule createValueRule() {
        final var value = new LazyRule();
        value.setChildRule(new OrRule(List.of(
                createInvocationRule(value),
                createAccessRule(value),
                createSymbolRule()
        )));
        return value;
    }

    private static TypeRule createSymbolRule() {
        return new TypeRule("symbol", new FilterRule(new SymbolFilter(), new ExtractRule("content")));
    }

    private static TypeRule createAccessRule(LazyRule value) {
        final var parent = new NodeRule("parent", value);
        final var child = new FilterRule(new SymbolFilter(), new ExtractRule("child"));
        return new TypeRule("access", new LocatingRule(parent, new LastLocator("."), child));
    }

    private static TypeRule createInvocationRule(LazyRule value) {
        return new TypeRule("invocation", new LocatingRule(new NodeRule("caller", value), new FirstLocator("("), new SuffixRule(new NodeListRule(new ValueSplitter(), "arguments", value), ")")));
    }

    private static Rule createTypeRule() {
        final var type = new LazyRule();
        type.setChildRule(new OrRule(List.of(
                createGenericRule(type),
                new TypeRule("symbol", new StripRule(new FilterRule(new SymbolFilter(), new ExtractRule("value")), "", ""))
        )));
        return type;
    }

    private static TypeRule createGenericRule(LazyRule type) {
        final var base = new NodeRule("base", type);
        final var child = new NodeListRule(new ValueSplitter(), "children", type);

        return new TypeRule("generic", new StripRule(new LocatingRule(base, new FirstLocator("<"), new SuffixRule(child, ">")), "", ""));
    }
}
