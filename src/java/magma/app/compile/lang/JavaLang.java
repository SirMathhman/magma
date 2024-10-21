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
        final var simpleName = new ExtractRule(NAME);
        final var typeParams = new NodeListRule(new ValueSplitter(), "type-params", createTypeRule());
        final var name = new OptionalNodeRule("type-params", new LocatingRule(simpleName, new FirstLocator("<"), new SuffixRule(typeParams, ">")), simpleName);

        final var params = new NodeListRule(new ValueSplitter(), "params", createDefinitionRule());
        final var content = new NodeListRule(new StatementSplitter(), "children", new StripRule(createClassMemberRule(), "", ""));

        final var anInterface = new NodeRule("interface", createTypeRule());
        final var implementsPresent = new StripRule(new PrefixRule("implements", new LocatingRule(anInterface, new FirstLocator("{"), new SuffixRule(content, "}"))), "", "");
        final var implementsEmpty = new StripRule(new PrefixRule("{", new SuffixRule(content, "}")), "", "");
        final var afterParams = new OptionalNodeRule("super", implementsPresent, implementsEmpty);
        final var afterKeyword = new LocatingRule(name, new FirstLocator("("), new LocatingRule(params, new FirstLocator(")"), afterParams));
        return new TypeRule(RECORD, new LocatingRule(createModifiersRule(), new FirstLocator("record "), afterKeyword));
    }

    private static TypeRule createDefinitionRule() {
        return new TypeRule("definition", new LocatingRule(new ExtractRule("type"), new LastLocator(" "), new ExtractRule("name")));
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

    private static StringListRule createModifiersRule() {
        return new StringListRule(MODIFIERS, " ");
    }

    private static TypeRule createMethodRule() {
        final var type = createTypeRule();
        final var name = new ExtractRule("name");
        final var returns = new NodeRule("returns", type);

        final var withModifiers = new LocatingRule(createModifiersRule(), new LastLocator(" "), returns);
        final var maybeModifiers = new OptionalNodeRule("modifiers", withModifiers, returns);

        final var annotation = new StripRule(new PrefixRule("@", new ExtractRule("annotation")), "", "");
        final var withAnnotations = new LocatingRule(new NodeListRule(new SimpleSplitter(" "), "annotations", annotation), new LastLocator("\n"), maybeModifiers);
        final var maybeAnnotations = new OptionalNodeRule("annotations", withAnnotations, maybeModifiers);

        final var beforeParams = new LocatingRule(maybeAnnotations, new LastLocator(" "), name);
        final var params = new OptionalNodeRule("params", new NodeRule("params", new TypeRule("content", new ExtractRule("params"))), new EmptyRule());

        final var children = new StripRule(new PrefixRule("{", new SuffixRule(new NodeListRule(new StatementSplitter(), "children", new StripRule(createStatementRule(), "", "")), "}")), "", "");
        final var maybeChildren = new OptionalNodeRule("children", children, new SuffixRule(new EmptyRule(), ";"));
        final var withParams = new LocatingRule(params, new FirstLocator(")"), maybeChildren);

        return new TypeRule(METHOD, new LocatingRule(beforeParams, new FirstLocator("("), withParams));
    }

    private static Rule createStatementRule() {
        return new OrRule(List.of(
                new TypeRule("return", new PrefixRule("return ", new SuffixRule(new NodeRule("value", createValueRule()), ";"))),
                new TypeRule("symbol", new ExtractRule("content"))
        ));
    }

    private static Rule createValueRule() {
        final var value = new LazyRule();
        value.setChildRule(new OrRule(List.of(
                createInvocationRule(value),
                new TypeRule("access", new LocatingRule(new NodeRule("parent", value), new LastLocator("."), new ExtractRule("name"))),
                new TypeRule("symbol", new ExtractRule("content"))
        )));
        return value;
    }

    private static TypeRule createInvocationRule(LazyRule value) {
        return new TypeRule("invocation", new LocatingRule(new NodeRule("caller", value), new FirstLocator("("), new SuffixRule(new NodeListRule(new ValueSplitter(), "arguments", value), ")")));
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
