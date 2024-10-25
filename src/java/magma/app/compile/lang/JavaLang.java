package magma.app.compile.lang;

import magma.app.compile.rule.*;

import java.util.List;

import static magma.app.compile.lang.CommonLang.*;

public class JavaLang {
    public static final String PACKAGE_TYPE = "package";
    public static final String RECORD_TYPE = "record";
    public static final String CLASS_TYPE = "class";
    public static final String INTERFACE_TYPE = "interface";
    public static final String METHOD = "method";

    public static final List<String> MODIFIERS_LIST = List.of(
            "public",
            "private",
            "static",
            "final",
            "default"
    );

    public static final String IMPORT_STATIC_TYPE = "import-static";

    public static TypeRule createPackageRule() {
        return new TypeRule(PACKAGE_TYPE, new PrefixRule("package ", new SuffixRule(createNamespaceRule(), STATEMENT_END)));
    }

    public static Rule createRootRule() {
        return CommonLang.createRootRule(createRootMemberRule());
    }

    public static TypeRule createRecordRule() {
        final var simpleName = new ExtractRule(NAME);
        final var typeParams = new NodeListRule(new ValueSplitter(), "type-params", createTypeRule());
        final var name = new OptionalNodeRule("type-params", simpleName, new LocatingRule(simpleName, new FirstLocator("<"), new SuffixRule(typeParams, ">")));

        final var params = new NodeListRule(new ValueSplitter(), "params", createDefinitionRule());
        final var withChildren = createChildrenRule(createClassMemberRule());
        final var maybeChildren = new StripRule(new OptionalNodeRule("children", new EmptyRule(), withChildren));

        final var anInterface = new NodeRule("interface", createTypeRule());
        final var implementsPresent = new StripRule(new PrefixRule("implements", new LocatingRule(anInterface, new FirstLocator("{"), new SuffixRule(maybeChildren, "}"))));
        final var implementsEmpty = new StripRule(new PrefixRule("{", new SuffixRule(maybeChildren, "}")));
        final var afterParams = new OptionalNodeRule("super", implementsEmpty, implementsPresent);
        final var afterKeyword = new LocatingRule(name, new FirstLocator("("), new LocatingRule(params, new FirstLocator(")"), afterParams));
        return new TypeRule(RECORD_TYPE, new LocatingRule(createModifiersRule(MODIFIERS_LIST), new FirstLocator("record "), afterKeyword));
    }

    private static TypeRule createDefinitionRule() {
        final var content = new NodeRule("returns", createTypeRule());
        final var typeParams = new StripRule(new PrefixRule("<", new SuffixRule(new ExtractRule("type-params"), ">")));
        final var withTypeParams = new ContextRule("With type params", new LocatingRule(typeParams, new ForwardsLocator(" "), content));
        final var withoutTypeParams = new ContextRule("Without type params", content);
        final var maybeTypeParams = new OptionalNodeRule("type-params", withoutTypeParams, withTypeParams);

        final var withModifiers = new ContextRule("With modifiers", new LocatingRule(createModifiersRule(MODIFIERS_LIST), new ForwardsLocator(" "), maybeTypeParams));
        final var withoutModifiers = new ContextRule("Without modifiers", maybeTypeParams);
        final var maybeModifiers = new StripRule(new OptionalNodeRule("modifiers", withoutModifiers, withModifiers));

        final var annotation = new TypeRule("annotation", new StripRule(new PrefixRule("@", new ExtractRule("value"))));
        final var annotations = new NodeListRule(new SimpleSplitter("\n"), "annotations", annotation);
        final var withAnnotations = new ContextRule("With annotations", new LocatingRule(annotations, new LastLocator("\n"), maybeModifiers));
        final var withoutAnnotations = new ContextRule("Without annotations", maybeModifiers);
        final var maybeAnnotations = new OptionalNodeRule("annotations", withoutAnnotations, withAnnotations);

        return new TypeRule("definition", new StripRule(new LocatingRule(maybeAnnotations, new LastLocator(" "), new ExtractRule("name"))));
    }

    private static OrRule createRootMemberRule() {
        return new OrRule(List.of(
                createPackageRule(),
                createImportRule(),
                createImportRule(IMPORT_STATIC_TYPE, "import static "),
                createRecordRule(),
                createInterfaceRule(),
                createClassRule(),
                createWhitespaceRule()
        ));
    }

    private static TypeRule createClassRule() {
        final var body = createChildrenRule(createClassMemberRule());
        final var afterKeyword = new LocatingRule(new ExtractRule("name"), new FirstLocator("{"), new SuffixRule(body, "}"));
        return new TypeRule(CLASS_TYPE, new LocatingRule(createModifiersRule(MODIFIERS_LIST), new FirstLocator("class "), afterKeyword));
    }

    private static TypeRule createInterfaceRule() {
        final var memberRule = createClassMemberRule();
        final var content = new SuffixRule(new NodeListRule(new StatementSplitter(), "content", new StripRule(memberRule)), "}");

        final var name = new StripRule(new FilterRule(new SymbolFilter(), new ExtractRule("name")));
        final var withTypeParams = new LocatingRule(name, new FirstLocator("<"), new StripRule(new SuffixRule(new NodeListRule(new ValueSplitter(), "type-params", createTypeRule()), ">")));
        final var maybeTypeParams = new OptionalNodeListRule("type-params", withTypeParams, name);

        final var nameAndContent = new LocatingRule(maybeTypeParams, new FirstLocator("{"), new StripRule(content));
        return new TypeRule(INTERFACE_TYPE, new LocatingRule(createModifiersRule(MODIFIERS_LIST), new FirstLocator("interface"), nameAndContent));
    }

    private static OrRule createClassMemberRule() {
        return new OrRule(List.of(
                createMethodRule(),
                createDeclarationRule(createValueRule()),
                createDefinitionStatementRule(),
                createWhitespaceRule()
        ));
    }

    private static TypeRule createDefinitionStatementRule() {
        return new TypeRule("definition", new StripRule(new SuffixRule(createDefinitionRule(), ";")));
    }

    private static TypeRule createMethodRule() {
        final var params = new OptionalNodeRule("params", new EmptyRule(), new NodeListRule(new ValueSplitter(), "params", createDefinitionRule()));

        final var children = new StripRule(new PrefixRule("{", new SuffixRule(createChildrenRule(createStatementRule()), "}")));
        final var maybeChildren = new OptionalNodeRule("children", new SuffixRule(new EmptyRule(), ";"), children);
        final var withParams = new LocatingRule(params, new FirstLocator(")"), maybeChildren);

        return new TypeRule(METHOD, new LocatingRule(createDefinitionRule(), new FirstLocator("("), withParams));
    }

    private static Rule createStatementRule() {
        final var valueRule = createValueRule();

        final var statement = new LazyRule();
        statement.setChildRule(new OrRule(List.of(
                createWhitespaceRule(),
                createReturnRule(valueRule),
                createInvocationStatementRule(valueRule),
                createConditionRule("if", "if"),
                createConditionRule("while", "while"),
                createDeclarationRule(valueRule),
                createNamedBlockRule(statement, "else", "else"),
                createNamedBlockRule(statement, "try", "try"),
                createConditionRule("catch", "catch"),
                createAssignmentRule(valueRule),
                createPostRule("increment", valueRule, "++"),
                createPostRule("decrement", valueRule, "--")
        )));
        return statement;
    }

    private static TypeRule createPostRule(String type, Rule valueRule, String operator) {
        return new TypeRule("post-" + type, new StripRule(new SuffixRule(new NodeRule("value", valueRule), operator + ";")));
    }

    private static TypeRule createAssignmentRule(Rule valueRule) {
        return new TypeRule("assignment", new StripRule(new LocatingRule(valueRule, new FirstLocator("="), new SuffixRule(valueRule, ";"))));
    }

    private static TypeRule createNamedBlockRule(Rule statement, String type, String prefix) {
        return new TypeRule(type, new PrefixRule(prefix, new StripRule(new PrefixRule("{", new SuffixRule(createChildrenRule(statement), "}")))));
    }

    private static TypeRule createDeclarationRule(Rule valueRule) {
        return new TypeRule("declaration", new LocatingRule(createDefinitionRule(), new FirstLocator("="), new SuffixRule(valueRule, ";")));
    }

    private static TypeRule createConditionRule(String type, String prefix) {
        return new TypeRule(type, new PrefixRule(prefix, new ExtractRule("content")));
    }

    private static TypeRule createInvocationStatementRule(Rule valueRule) {
        return new TypeRule("invocation", new SuffixRule(createInvocationRule(valueRule), ";"));
    }

    private static TypeRule createReturnRule(Rule valueRule) {
        return new TypeRule("return", new PrefixRule("return ", new SuffixRule(new NodeRule("value", valueRule), ";")));
    }

    private static Rule createValueRule() {
        final var value = new LazyRule();
        value.setChildRule(new OrRule(List.of(
                new TypeRule("char", new StripRule(new PrefixRule("'", new SuffixRule(new ExtractRule("value"), "'")))),
                createQuantityRule(value),
                createStringRule(),
                createConstructionRule(value),
                createInvocationRule(value),
                createSymbolRule(),
                createNumberRule(),
                createOperationRule(value, "addition", "+"),
                createOperationRule(value, "subtraction", "-"),
                createOperationRule(value, "equals", "=="),
                createOperationRule(value, "not-equals", "!="),
                createOperationRule(value, "or", "||"),
                createOperationRule(value, "and", "&&"),
                createTernaryRule(value),
                createAccessRule(value, "property-access", "."),
                createAccessRule(value, "method-access", "::"),
                createLambdaRule())));
        return value;
    }

    private static TypeRule createQuantityRule(LazyRule value) {
        return new TypeRule("quantity", new StripRule(new PrefixRule("(", new SuffixRule(new NodeRule("value", value), ")"))));
    }

    private static TypeRule createStringRule() {
        final var value = new OrRule(List.of(new ExtractRule("value"), new EmptyRule()));
        return new TypeRule("string", new StripRule(new PrefixRule("\"", new SuffixRule(value, "\""))));
    }

    private static TypeRule createLambdaRule() {
        return new TypeRule("lambda", new LocatingRule(new ExtractRule("before-arrow"), new FirstLocator("->"), new ExtractRule("after-arrow")));
    }

    private static TypeRule createTernaryRule(LazyRule value) {
        final var condition = new NodeRule("condition", value);
        final var ifTrue = new NodeRule("ifTrue", value);
        final var ifFalse = new NodeRule("ifFalse", value);
        final var afterCondition = new LocatingRule(ifTrue, new FirstLocator(":"), ifFalse);
        return new TypeRule("ternary", new LocatingRule(condition, new FirstLocator("?"), afterCondition));
    }

    private static Rule createNumberRule() {
        return new TypeRule("number", new StripRule(new FilterRule(new NumberFilter(), new ExtractRule("value"))));
    }

    private static TypeRule createOperationRule(Rule value, String type, String operator) {
        final var left = new NodeRule("left", value);
        final var right = new NodeRule("right", value);
        return new TypeRule(type, new LocatingRule(left, new FirstLocator(operator), right));
    }

    private static TypeRule createSymbolRule() {
        return new TypeRule("symbol", new StripRule(new FilterRule(new SymbolFilter(), new ExtractRule("content"))));
    }

    private static TypeRule createAccessRule(LazyRule value, String type, String separator) {
        final var parent = new NodeRule("parent", value);
        final var child = new StripRule(new FilterRule(new SymbolFilter(), new ExtractRule("child")));
        return new TypeRule(type, new LocatingRule(parent, new LastLocator(separator), child));
    }

    private static TypeRule createConstructionRule(Rule value) {
        final var caller = new NodeRule("caller", value);
        final var typeArguments = new OptionalNodeRule("type-arguments", new EmptyRule(), new NodeListRule(new ValueSplitter(), "type-arguments", createTypeRule()));
        final var withTypeArguments = new StripRule(new LocatingRule(caller, new FirstLocator("<"), new SuffixRule(typeArguments, ">")));

        final var beforeParams = new PrefixRule("new ", new OptionalNodeRule("type-arguments", caller, withTypeArguments));
        final var arguments = createArgumentsRule(value);
        return new TypeRule("construction", new StripRule(new LocatingRule(beforeParams, new FirstLocator("("), new SuffixRule(arguments, ")"))));
    }

    private static TypeRule createInvocationRule(Rule value) {
        final var caller = new NodeRule("caller", value);
        final var arguments = createArgumentsRule(value);
        return new TypeRule("invocation", new StripRule(new LocatingRule(caller, new OpeningLocator(), new SuffixRule(arguments, ")"))));
    }

    private static OptionalNodeListRule createArgumentsRule(Rule value) {
        return new OptionalNodeListRule("arguments", new NodeListRule(new ValueSplitter(), "arguments", value), new EmptyRule());
    }

    private static Rule createTypeRule() {
        final var type = new LazyRule();
        type.setChildRule(new OrRule(List.of(
                createGenericRule(type),
                new TypeRule("array", new StripRule(new SuffixRule(new NodeRule("child", type), "[]"))),
                new TypeRule("symbol", new StripRule(createNamespaceRule())))));
        return type;
    }

    private static TypeRule createGenericRule(LazyRule type) {
        final var base = new NodeRule("base", type);
        final var child = new NodeListRule(new ValueSplitter(), "children", type);

        return new TypeRule("generic", new StripRule(new LocatingRule(base, new FirstLocator("<"), new SuffixRule(child, ">"))));
    }
}
