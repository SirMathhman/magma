package magma.app.lang;

import magma.app.locate.BackwardsLocator;
import magma.app.locate.InvocationLocator;
import magma.app.rule.ContextRule;
import magma.app.rule.ExactRule;
import magma.app.rule.FilterRule;
import magma.app.rule.InfixRule;
import magma.app.rule.LazyRule;
import magma.app.rule.NodeRule;
import magma.app.rule.OptionalNodeListRule;
import magma.app.rule.OptionalNodeRule;
import magma.app.rule.OrRule;
import magma.app.rule.PrefixRule;
import magma.app.rule.Rule;
import magma.app.rule.StringRule;
import magma.app.rule.StripRule;
import magma.app.rule.SuffixRule;
import magma.app.rule.TypeRule;
import magma.app.rule.divide.DivideRule;
import magma.app.rule.divide.SimpleDivider;
import magma.app.rule.filter.NumberFilter;
import magma.app.rule.filter.SymbolFilter;
import magma.app.rule.locate.FirstLocator;
import magma.app.rule.locate.LastLocator;
import magma.app.rule.locate.ParenthesesMatcher;

import java.util.List;

import static magma.app.rule.divide.StatementDivider.STATEMENT_DIVIDER;
import static magma.app.rule.divide.ValueDivider.VALUE_DIVIDER;

public class CommonLang {
    public static final String ROOT_TYPE = "root";
    public static final String RECORD_TYPE = "record";
    public static final String CLASS_TYPE = "class";
    public static final String INTERFACE_TYPE = "interface";
    public static final String BEFORE_STRUCT_SEGMENT = "before-struct-segment";
    public static final String STRUCT_TYPE = "struct";
    public static final String WHITESPACE_TYPE = "whitespace";
    public static final String CONTENT_BEFORE_CHILD = "content-before-child";
    public static final String GENERIC_CONSTRUCTOR = "caller";
    public static final String GENERIC_CHILDREN = "generic-children";
    public static final String FUNCTIONAL_TYPE = "functional";
    public static final String METHOD_VALUE = "value";
    public static final String DEFINITION_ANNOTATIONS = "annotations";
    public static final String METHOD_TYPE = "method";
    public static final String INITIALIZATION_TYPE = "initialization";
    public static final String METHOD_DEFINITION = "definition";
    public static final String INITIALIZATION_VALUE = "value";
    public static final String INITIALIZATION_DEFINITION = "definition";
    public static final String DEFINITION_TYPE = "definition";
    public static final String TUPLE_TYPE = "tuple";
    public static final String TUPLE_CHILDREN = "tuple-children";
    public static final String CONTENT_AFTER_CHILD = "after-child";
    public static final String CONTENT_CHILDREN = "children";
    public static final String INVOCATION_CHILDREN = "invocation-children";
    public static final String CONTENT_AFTER_CHILDREN = "content-after-children";
    public static final String GENERIC_TYPE = "generic";
    public static final String METHOD_PARAMS = "params";
    public static final String FUNCTIONAL_PARAMS = "params";
    public static final String FUNCTIONAL_RETURN = "return";

    public static Rule createNamespacedRule(String type, String prefix) {
        final var namespace = new StringRule("namespace");
        final var childRule = new PrefixRule(prefix, new SuffixRule(namespace, ";"));
        return new TypeRule(type, new StripRule(childRule));
    }

    public static Rule createCompoundRule(String type, String infix, Rule segmentRule) {
        final var modifiers = createModifiersRule();
        final var maybeModifiers = new OptionalNodeRule("modifiers",
                new SuffixRule(modifiers, " ")
        );

        final var name = new StripRule(new FilterRule(new SymbolFilter(), new StringRule("name")));
        final var typeParams = new DivideRule("type-params", VALUE_DIVIDER, createTypeRule());
        final var maybeTypeParams = new OptionalNodeListRule("type-params",
                new InfixRule(name, new FirstLocator("<"), new StripRule(new SuffixRule(typeParams, ">"))),
                name
        );

        final var params = new DivideRule("params", VALUE_DIVIDER, createDefinitionRule());
        final var maybeParams = new OptionalNodeListRule("params",
                new InfixRule(maybeTypeParams, new FirstLocator("("), new StripRule(new SuffixRule(params, ")"))),
                maybeTypeParams
        );

        final var supertype = new NodeRule("supertype", createTypeRule());
        final var maybeImplements = new OptionalNodeRule("supertype",
                new InfixRule(maybeParams, new FirstLocator(" implements "), supertype),
                maybeParams
        );

        final var nameAndContent = wrapUsingBlock("value", maybeImplements, segmentRule);
        final var infixRule = new InfixRule(maybeModifiers, new FirstLocator(infix), nameAndContent);
        return new TypeRule(type, infixRule);
    }

    public static StripRule createStructSegmentRule(LazyRule function, Rule statement, LazyRule struct) {
        return new StripRule(new OrRule(List.of(
                function,
                createInitializationRule(createValueRule(statement, function)),
                createDefinitionStatementRule(),
                createWhitespaceRule(),
                struct
        )), BEFORE_STRUCT_SEGMENT, "");
    }

    private static SuffixRule createDefinitionStatementRule() {
        return new SuffixRule(createDefinitionRule(), ";");
    }

    private static Rule createInitializationRule(Rule value) {
        final var definition = new NodeRule(INITIALIZATION_DEFINITION, createDefinitionRule());
        final var valueRule = new NodeRule(INITIALIZATION_VALUE, value);
        final var infixRule = new InfixRule(definition, new FirstLocator("="), new StripRule(new SuffixRule(valueRule, ";")));
        return new TypeRule(INITIALIZATION_TYPE, infixRule);
    }

    static Rule createMethodRule(Rule statement) {
        final var definition = createDefinitionRule();
        final var definitionProperty = new NodeRule(METHOD_DEFINITION, definition);
        final var params = new OptionalNodeListRule(METHOD_PARAMS, new DivideRule(METHOD_PARAMS, VALUE_DIVIDER, definition));
        final var infixRule = new InfixRule(definitionProperty, new FirstLocator("("), new StripRule(new SuffixRule(params, ")")));

        final var orRule = new OptionalNodeRule(METHOD_VALUE,
                new ContextRule("With block", wrapUsingBlock(METHOD_VALUE, infixRule, statement)),
                new ContextRule("With statement", new StripRule(new SuffixRule(infixRule, ";")))
        );

        return new TypeRule(METHOD_TYPE, orRule);
    }

    private static Rule wrapUsingBlock(String propertyKey, Rule beforeBlock, Rule statement) {
        final var withEnd = new NodeRule(propertyKey, new TypeRule("block", createContentRule(statement)));
        return new StripRule(new InfixRule(beforeBlock, new FirstLocator("{"), new SuffixRule(withEnd, "}")));
    }

    public static Rule createContentRule(Rule rule) {
        return new StripRule(new OptionalNodeListRule(CONTENT_CHILDREN,
                new DivideRule(CONTENT_CHILDREN, STATEMENT_DIVIDER, new StripRule(rule, CONTENT_BEFORE_CHILD, CONTENT_AFTER_CHILD))
        ), "", CONTENT_AFTER_CHILDREN);
    }

    static Rule createStatementRule(Rule function, LazyRule struct) {
        final var statement = new LazyRule();
        final var valueRule = createValueRule(statement, function);
        statement.set(new OrRule(List.of(
                createKeywordRule("continue"),
                createKeywordRule("break"),
                createInitializationRule(createValueRule(statement, function)),
                createDefinitionStatementRule(),
                createConditionalRule(statement, "if", createValueRule(statement, function)),
                createConditionalRule(statement, "while", createValueRule(statement, function)),
                createElseRule(statement),
                createInvocationStatementRule(valueRule),
                createReturnRule(valueRule),
                createAssignmentRule(valueRule),
                createPostfixRule("post-increment", "++", valueRule),
                createPostfixRule("post-decrement", "--", valueRule),
                createWhitespaceRule()
        )));
        return statement;
    }

    private static TypeRule createKeywordRule(String keyword) {
        return new TypeRule(keyword, new StripRule(new ExactRule(keyword + ";")));
    }

    private static TypeRule createElseRule(LazyRule statement) {
        return new TypeRule("else", new OrRule(List.of(
                wrapUsingBlock("value", new StripRule(new ExactRule("else")), statement),
                new PrefixRule("else ", new NodeRule("value", statement))
        )));
    }

    private static TypeRule createConditionalRule(LazyRule statement, String type, Rule value) {
        final var condition = new NodeRule("condition", value);

        return new TypeRule(type, new StripRule(new PrefixRule(type, new OrRule(List.of(
                new ContextRule("With block", wrapUsingBlock("value", new StripRule(new PrefixRule("(", new SuffixRule(condition, ")"))), statement)),
                new ContextRule("With statement", new StripRule(new PrefixRule("(", new InfixRule(condition, new ParenthesesMatcher(), new NodeRule("value", statement)))))
        )))));
    }

    public static TypeRule createWhitespaceRule() {
        return new TypeRule(WHITESPACE_TYPE, new StripRule(new ExactRule("")));
    }

    private static Rule createPostfixRule(String type, String operator, Rule value) {
        return new TypeRule(type, new SuffixRule(new NodeRule(INITIALIZATION_VALUE, value), operator + ";"));
    }

    private static Rule createAssignmentRule(Rule value) {
        final var destination = new NodeRule("destination", value);
        final var source = new NodeRule("source", value);
        return new TypeRule("assignment", new SuffixRule(new InfixRule(destination, new FirstLocator("="), source), ";"));
    }

    private static Rule createInvocationStatementRule(Rule value) {
        return new SuffixRule(createInvocationRule(value), ";");
    }

    private static TypeRule createInvocationRule(Rule value) {
        final var caller = new NodeRule("caller", value);
        final var children = new OptionalNodeListRule(INVOCATION_CHILDREN,
                new DivideRule(INVOCATION_CHILDREN, VALUE_DIVIDER, value)
        );

        final var suffixRule = new StripRule(new SuffixRule(new InfixRule(caller, new InvocationLocator(), children), ")")
        );
        return new TypeRule("invocation", suffixRule);
    }

    private static Rule createReturnRule(Rule value) {
        return new TypeRule("return", new StripRule(new PrefixRule("return ", new SuffixRule(new NodeRule(INITIALIZATION_VALUE, value), ";"))));
    }

    private static Rule createValueRule(Rule statement, Rule function) {
        final var value = new LazyRule();
        value.set(new OrRule(List.of(
                createLambdaRule(statement, value),
                function,
                createConstructionRule(value),
                createInvocationRule(value),
                createAccessRule("data-access", ".", value),
                createAccessRule("method-access", "::", value),
                createSymbolRule(),
                createNumberRule(),
                createNotRule(value),
                createOperatorRule("greater-equals", ">=", value),
                createOperatorRule("less", "<", value),
                createOperatorRule("equals", "==", value),
                createOperatorRule("and", "&&", value),
                createOperatorRule("add", "+", value),
                createCharRule(),
                createStringRule(),
                createTernaryRule(value)
        )));

        return value;
    }

    private static TypeRule createLambdaRule(Rule statement, LazyRule value) {
        final var args = new StripRule(new OrRule(List.of(
                new ExactRule("()"),
                new NodeRule("arg", createSymbolRule()),
                new DivideRule("args", new SimpleDivider(","), createSymbolRule())
        )));

        final var rightRule = new OrRule(List.of(
                new NodeRule("value", wrapUsingBlock("value", new StripRule(new SuffixRule(args, "->")), statement)),
                new InfixRule(args, new FirstLocator("->"), new NodeRule("value", value))
        ));

        return new TypeRule("lambda", rightRule);
    }

    private static TypeRule createStringRule() {
        final var value = new PrefixRule("\"", new SuffixRule(new StringRule(INITIALIZATION_VALUE), "\""));
        return new TypeRule("string", new StripRule(value));
    }

    private static TypeRule createTernaryRule(LazyRule value) {
        return new TypeRule("ternary", new InfixRule(new NodeRule("condition", value), new FirstLocator("?"), new InfixRule(new NodeRule("ifTrue", value), new FirstLocator(":"), new NodeRule("ifElse", value))));
    }

    private static TypeRule createCharRule() {
        return new TypeRule("char", new StripRule(new PrefixRule("'", new SuffixRule(new StringRule(INITIALIZATION_VALUE), "'"))));
    }

    private static TypeRule createNumberRule() {
        return new TypeRule("number", new StripRule(new FilterRule(new NumberFilter(), new StringRule(INITIALIZATION_VALUE))));
    }

    private static TypeRule createOperatorRule(String type, String operator, LazyRule value) {
        return new TypeRule(type, new InfixRule(new NodeRule("left", value), new FirstLocator(operator), new NodeRule("right", value)));
    }

    private static TypeRule createNotRule(LazyRule value) {
        return new TypeRule("not", new StripRule(new PrefixRule("!", new NodeRule(INITIALIZATION_VALUE, value))));
    }

    private static TypeRule createConstructionRule(LazyRule value) {
        final var type = new StringRule("type");
        final var arguments = new OptionalNodeListRule("arguments",
                new DivideRule("arguments", VALUE_DIVIDER, value)
        );
        final var childRule = new InfixRule(type, new FirstLocator("("), new StripRule(new SuffixRule(arguments, ")")));
        return new TypeRule("construction", new StripRule(new PrefixRule("new ", childRule)));
    }

    private static Rule createSymbolRule() {
        return new TypeRule("symbol", new StripRule(new FilterRule(new SymbolFilter(), new StringRule("value"))));
    }

    private static Rule createAccessRule(String type, String infix, final Rule value) {
        final var rule = new InfixRule(new NodeRule("ref", value), new LastLocator(infix), new StringRule("property"));
        return new TypeRule(type, rule);
    }

    private static Rule createDefinitionRule() {
        final var name = new FilterRule(new SymbolFilter(), new StringRule("name"));
        final var typeProperty = new NodeRule("type", createTypeRule());
        final var typeAndName = new StripRule(new InfixRule(typeProperty, new LastLocator(" "), name));

        final var modifiers = createModifiersRule();

        final var typeParams = new StringRule("type-params");
        final var maybeTypeParams = new OrRule(List.of(
                new ContextRule("With type params", new InfixRule(new StripRule(new PrefixRule("<", typeParams)), new FirstLocator(">"), new StripRule(typeAndName))),
                new ContextRule("Without type params", typeAndName)
        ));

        final var withModifiers = new OptionalNodeListRule("modifiers",
                new ContextRule("With modifiers", new StripRule(new InfixRule(modifiers, new BackwardsLocator(" "), maybeTypeParams))),
                new ContextRule("Without modifiers", maybeTypeParams)
        );

        final var annotation = new TypeRule("annotation", new StripRule(new PrefixRule("@", new StringRule(INITIALIZATION_VALUE))));
        final var annotations = new DivideRule(DEFINITION_ANNOTATIONS, new SimpleDivider("\n"), annotation);
        return new TypeRule(DEFINITION_TYPE, new OrRule(List.of(
                new ContextRule("With annotations", new InfixRule(annotations, new LastLocator("\n"), withModifiers)),
                new ContextRule("Without annotations", withModifiers)
        )));
    }

    private static DivideRule createModifiersRule() {
        final var modifierRule = new TypeRule("modifier", new StripRule(new FilterRule(new SymbolFilter(), new StringRule(INITIALIZATION_VALUE))));
        return new DivideRule("modifiers", new SimpleDivider(" "), modifierRule);
    }

    private static Rule createTypeRule() {
        final var type = new LazyRule();
        type.set(new OrRule(List.of(
                createSymbolRule(),
                createGenericRule(type),
                createVarArgsRule(type),
                createArrayRule(type),
                createFunctionalRule(type),
                createTupleRule(type),
                createSliceRule(type),
                createStructRule(),
                new TypeRule("ref", new SuffixRule(new NodeRule("value", type), "*"))
        )));

        return type;
    }

    private static TypeRule createStructRule() {
        return new TypeRule("struct", new PrefixRule("struct ", new StringRule("value")));
    }

    private static TypeRule createSliceRule(LazyRule type) {
        return new TypeRule("slice", new PrefixRule("&[", new SuffixRule(new NodeRule("child", type), "]")));
    }

    private static TypeRule createTupleRule(LazyRule type) {
        return new TypeRule(TUPLE_TYPE, new PrefixRule("[", new SuffixRule(new DivideRule(TUPLE_CHILDREN, VALUE_DIVIDER, type), "]")));
    }

    private static TypeRule createFunctionalRule(Rule type) {
        final var params = new OptionalNodeListRule(FUNCTIONAL_PARAMS, new DivideRule(FUNCTIONAL_PARAMS, VALUE_DIVIDER, type));
        final var leftRule = new PrefixRule("(", new SuffixRule(params, ")"));
        final var rule = new InfixRule(new NodeRule(FUNCTIONAL_RETURN, type), new FirstLocator(" (*)"), leftRule);
        return new TypeRule(FUNCTIONAL_TYPE, rule);
    }

    private static TypeRule createArrayRule(LazyRule type) {
        return new TypeRule("array", new SuffixRule(new NodeRule(METHOD_VALUE, type), "[]"));
    }

    private static TypeRule createVarArgsRule(LazyRule type) {
        return new TypeRule("var-args", new SuffixRule(new NodeRule(METHOD_VALUE, type), "..."));
    }

    private static TypeRule createGenericRule(LazyRule type) {
        final var parent = new StringRule(GENERIC_CONSTRUCTOR);
        final var children = new DivideRule(GENERIC_CHILDREN, VALUE_DIVIDER, type);
        return new TypeRule(GENERIC_TYPE, new InfixRule(new StripRule(parent), new FirstLocator("<"), new StripRule(new SuffixRule(children, ">"))));
    }

}
