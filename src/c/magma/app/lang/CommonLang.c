import magma.app.locate.BackwardsLocator;
import magma.app.locate.InvocationLocator;
import magma.app.rule.ContextRule;
import magma.app.rule.ExactRule;
import magma.app.rule.FilterRule;
import magma.app.rule.InfixRule;
import magma.app.rule.LazyRule;
import magma.app.rule.NodeRule;
import magma.app.rule.OptionalNodeListRule;
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
public struct CommonLang {
	public static final String NAMESPACED_BEFORE="before";
	public static final String NAMESPACED_AFTER="after";
	public static final String ROOT_TYPE="root";
	public static final String RECORD_TYPE="record";
	public static final String CLASS_TYPE="class";
	public static final String INTERFACE_TYPE="interface";
	public static final String BEFORE_STRUCT_SEGMENT="before-struct-segment";
	public static final String STRUCT_TYPE="struct";
	public static final String WHITESPACE_TYPE="whitespace";
	public static final String STRUCT_AFTER_CHILDREN="struct-after-children";
	public static final String BLOCK_AFTER_CHILDREN="block-after-children";
	public static final String BLOCK="block";
	public static final String CONTENT_BEFORE_CHILD="before-child";
	public static final String PARENT="caller";
	public static final String GENERIC_CHILDREN="children";
	public static final String FUNCTIONAL_TYPE="functional";
	public static final String METHOD_CHILD="child";
	public static final String DEFINITION_ANNOTATIONS="annotations";
	public static final String DEFINITION_MODIFIERS="modifiers";
	public static final String METHOD_TYPE="method";
	public static final String INITIALIZATION_TYPE="initialization";
	public static final String METHOD_DEFINITION=INITIALIZATION_TYPE;
	public static final String INITIALIZATION_VALUE="value";
	public static final String INITIALIZATION_DEFINITION="definition";
	public static final String DEFINITION_TYPE="definition";
	public static final String TUPLE_TYPE="tuple";
	public static final String TUPLE_CHILDREN="children";
	public static final String CONTENT_AFTER_CHILD="after-child";
	public static Rule createNamespacedRule(String type, String prefix){
	final var namespace=new StringRule("namespace");
	final var childRule=new PrefixRule(prefix, new SuffixRule(namespace, ";"));
	return new TypeRule(type, new StripRule(childRule));
}
	public static Rule createCompoundRule(String type, String infix, Rule segmentRule){
	final var infixRule=new InfixRule(new StringRule(DEFINITION_MODIFIERS), new FirstLocator(infix), new InfixRule(new StringRule("name"), new FirstLocator("{"), new StripRule(new SuffixRule(new StripRule(createContentRule(segmentRule), "", STRUCT_AFTER_CHILDREN), "}"))));
	return new TypeRule(type, infixRule);
}
	public static Rule createStructSegmentRule(){
	final var function=new LazyRule();
	final var statement=createStatementRule(function);
	function.set(createMethodRule(statement));
	return new StripRule(new OrRule(List.of(function, createInitializationRule(createValueRule(statement, function)), createDefinitionStatementRule(), createWhitespaceRule())), BEFORE_STRUCT_SEGMENT, "");
}
	private static SuffixRule createDefinitionStatementRule(){
	return new SuffixRule(createDefinitionRule(), ";");
}
	private static Rule createInitializationRule(Rule value){
	final var definition=new NodeRule(INITIALIZATION_DEFINITION, createDefinitionRule());
	final var valueRule=new NodeRule(INITIALIZATION_VALUE, value);
	final var infixRule=new InfixRule(definition, new FirstLocator("="), new StripRule(new SuffixRule(valueRule, ";")));
	return new TypeRule(INITIALIZATION_TYPE, infixRule);
}
	private static Rule createMethodRule(Rule statement){
	final var orRule=new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new ExactRule(";")));
	final var definition=createDefinitionRule();
	final var definitionProperty=new NodeRule(METHOD_DEFINITION, definition);
	final var params=new OptionalNodeListRule("params", new DivideRule("params", VALUE_DIVIDER, definition), new ExactRule(""));
	final var infixRule=new InfixRule(definitionProperty, new FirstLocator("("), new InfixRule(params, new FirstLocator(")"), orRule));
	return new TypeRule(METHOD_TYPE, infixRule);
}
	private static TypeRule createBlockRule(Rule statement){
	return new TypeRule(BLOCK, new StripRule(new PrefixRule("{", new SuffixRule(new StripRule(createContentRule(statement), "", BLOCK_AFTER_CHILDREN), "}"))));
}
	public static Rule createContentRule(Rule rule){
	return new OrRule(List.of(new DivideRule(GENERIC_CHILDREN, STATEMENT_DIVIDER, new StripRule(rule, CONTENT_BEFORE_CHILD, CONTENT_AFTER_CHILD)), new ExactRule("")));
}
	private static Rule createStatementRule(Rule function){
	final var statement=new LazyRule();
	final var valueRule=createValueRule(statement, function);
	statement.set(new OrRule(List.of(createKeywordRule("continue"), createKeywordRule("break"), createInitializationRule(createValueRule(statement, function)), createDefinitionStatementRule(), createConditionalRule(statement, "if", createValueRule(statement, function)), createConditionalRule(statement, "while", createValueRule(statement, function)), createElseRule(statement), createInvocationStatementRule(valueRule), createReturnRule(valueRule), createAssignmentRule(valueRule), createPostfixRule("post-increment", "++", valueRule), createPostfixRule("post-decrement", "--", valueRule), createWhitespaceRule())));
	return statement;
}
	private static TypeRule createKeywordRule(String keyword){
	return new TypeRule(keyword, new StripRule(new ExactRule(keyword+";")));
}
	private static TypeRule createElseRule(LazyRule statement){
	return new TypeRule("else", new StripRule(new PrefixRule("else ", new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new NodeRule(INITIALIZATION_VALUE, statement))))));
}
	private static TypeRule createConditionalRule(LazyRule statement, String type, Rule value){
	final var leftRule=new StripRule(new PrefixRule("(", new NodeRule("condition", value)));
	final var blockRule=new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new NodeRule(METHOD_CHILD, statement)));
	return new TypeRule(type, new PrefixRule(type, new InfixRule(leftRule, new ParenthesesMatcher(), blockRule)));
}
	public static TypeRule createWhitespaceRule(){
	return new TypeRule(WHITESPACE_TYPE, new StripRule(new ExactRule("")));
}
	private static Rule createPostfixRule(String type, String operator, Rule value){
	return new TypeRule(type, new SuffixRule(new NodeRule(INITIALIZATION_VALUE, value), operator+";"));
}
	private static Rule createAssignmentRule(Rule value){
	final var destination=new NodeRule("destination", value);
	final var source=new NodeRule("source", value);
	return new TypeRule("assignment", new SuffixRule(new InfixRule(destination, new FirstLocator("="), source), ";"));
}
	private static Rule createInvocationStatementRule(Rule value){
	return new SuffixRule(createInvocationRule(value), ";");
}
	private static TypeRule createInvocationRule(Rule value){
	final var caller=new NodeRule("caller", value);
	final var children=new OrRule(List.of(new DivideRule(GENERIC_CHILDREN, VALUE_DIVIDER, value), new ExactRule("")));
	final var suffixRule=new StripRule(new SuffixRule(new InfixRule(caller, new InvocationLocator(), children), ")"));
	return new TypeRule("invocation", suffixRule);
}
	private static Rule createReturnRule(Rule value){
	return new TypeRule("return", new StripRule(new PrefixRule("return ", new SuffixRule(new NodeRule(INITIALIZATION_VALUE, value), ";"))));
}
	private static Rule createValueRule(Rule statement, Rule function){
	final var value=new LazyRule();
	value.set(new OrRule(List.of(function, createConstructionRule(value), createInvocationRule(value), createAccessRule("data-access", ".", value), createAccessRule("method-access", "::", value), createSymbolRule(), createNumberRule(), createNotRule(value), createOperatorRule("greater-equals", ">=", value), createOperatorRule("less", "<", value), createOperatorRule("equals", "==", value), createOperatorRule("and", "&&", value), createOperatorRule("add", "+", value), createCharRule(), createStringRule(), createTernaryRule(value), createLambdaRule(statement, value))));
	return value;
}
	private static TypeRule createLambdaRule(Rule statement, LazyRule value){
	return new TypeRule("lambda", new InfixRule(new StringRule("args"), new FirstLocator("->"), new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new NodeRule(METHOD_CHILD, value)))));
}
	private static TypeRule createStringRule(){
	final var value=new PrefixRule("\"", new SuffixRule(new StringRule(INITIALIZATION_VALUE), "\""));
	return new TypeRule("string", new StripRule(value));
}
	private static TypeRule createTernaryRule(LazyRule value){
	return new TypeRule("ternary", new InfixRule(new NodeRule("condition", value), new FirstLocator("?"), new InfixRule(new NodeRule("ifTrue", value), new FirstLocator(":"), new NodeRule("ifElse", value))));
}
	private static TypeRule createCharRule(){
	return new TypeRule("char", new StripRule(new PrefixRule("'", new SuffixRule(new StringRule(INITIALIZATION_VALUE), "'"))));
}
	private static TypeRule createNumberRule(){
	return new TypeRule("number", new StripRule(new FilterRule(new NumberFilter(), new StringRule(INITIALIZATION_VALUE))));
}
	private static TypeRule createOperatorRule(String type, String operator, LazyRule value){
	return new TypeRule(type, new InfixRule(new NodeRule("left", value), new FirstLocator(operator), new NodeRule("right", value)));
}
	private static TypeRule createNotRule(LazyRule value){
	return new TypeRule("not", new StripRule(new PrefixRule("!", new NodeRule(INITIALIZATION_VALUE, value))));
}
	private static TypeRule createConstructionRule(LazyRule value){
	final var type=new StringRule("type");
	final var arguments=new OrRule(List.of(new DivideRule("arguments", VALUE_DIVIDER, value), new ExactRule("")));
	final var childRule=new InfixRule(type, new FirstLocator("("), new StripRule(new SuffixRule(arguments, ")")));
	return new TypeRule("construction", new StripRule(new PrefixRule("new ", childRule)));
}
	private static Rule createSymbolRule(){
	return new TypeRule("symbol", new StripRule(new FilterRule(new SymbolFilter(), new StringRule("value"))));
}
	private static Rule createAccessRule(String type, String infix, final Rule value){
	final var rule=new InfixRule(new NodeRule("ref", value), new LastLocator(infix), new StringRule("property"));
	return new TypeRule(type, rule);
}
	private static Rule createDefinitionRule(){
	final var name=new FilterRule(new SymbolFilter(), new StringRule("name"));
	final var typeProperty=new NodeRule("type", createTypeRule());
	final var typeAndName=new StripRule(new InfixRule(typeProperty, new LastLocator(" "), name));
	final var modifierRule=new TypeRule("modifier", new StripRule(new FilterRule(new SymbolFilter(), new StringRule(INITIALIZATION_VALUE))));
	final var modifiers=new DivideRule(DEFINITION_MODIFIERS, new SimpleDivider(" "), modifierRule);
	final var typeParams=new StringRule("type-params");
	final var maybeTypeParams=new OrRule(List.of(new ContextRule("With type params", new InfixRule(new StripRule(new PrefixRule("<", typeParams)), new FirstLocator(">"), new StripRule(typeAndName))), new ContextRule("Without type params", typeAndName)));
	final var withModifiers=new OrRule(List.of(new ContextRule("With modifiers", new StripRule(new InfixRule(modifiers, new BackwardsLocator(" "), maybeTypeParams))), new ContextRule("Without modifiers", maybeTypeParams)));
	final var annotation=new TypeRule("annotation", new StripRule(new PrefixRule("@", new StringRule(INITIALIZATION_VALUE))));
	final var annotations=new DivideRule(DEFINITION_ANNOTATIONS, new SimpleDivider("\n"), annotation);
	return new TypeRule(DEFINITION_TYPE, new OrRule(List.of(new ContextRule("With annotations", new InfixRule(annotations, new LastLocator("\n"), withModifiers)), new ContextRule("Without annotations", withModifiers))));
}
	private static Rule createTypeRule(){
	final var type=new LazyRule();
	type.set(new OrRule(List.of(createSymbolRule(), createGenericRule(type), createVarArgsRule(type), createArrayRule(type), createFunctionalType(type), new TypeRule(TUPLE_TYPE, new PrefixRule("[", new SuffixRule(new DivideRule(TUPLE_CHILDREN, VALUE_DIVIDER, type), "]"))))));
	return type;
}
	private static TypeRule createFunctionalType(Rule type){
	final var params=new OptionalNodeListRule("params", new DivideRule("params", VALUE_DIVIDER, type), new ExactRule(""));
	final var leftRule=new PrefixRule("(", new SuffixRule(params, ")"));
	final var rule=new InfixRule(leftRule, new FirstLocator(" => "), new NodeRule("return", type));
	return new TypeRule(FUNCTIONAL_TYPE, new PrefixRule("(", new SuffixRule(rule, ")")));
}
	private static TypeRule createArrayRule(LazyRule type){
	return new TypeRule("array", new SuffixRule(new NodeRule(METHOD_CHILD, type), "[]"));
}
	private static TypeRule createVarArgsRule(LazyRule type){
	return new TypeRule("var-args", new SuffixRule(new NodeRule(METHOD_CHILD, type), "..."));
}
	private static TypeRule createGenericRule(LazyRule type){
	return new TypeRule("generic", new InfixRule(new StripRule(new StringRule(PARENT)), new FirstLocator("<"), new SuffixRule(new DivideRule(GENERIC_CHILDREN, VALUE_DIVIDER, type), ">")));
}}

