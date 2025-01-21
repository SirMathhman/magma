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
struct CommonLang {
	static const String NAMESPACED_BEFORE="before";
	static const String NAMESPACED_AFTER="after";
	static const String ROOT_TYPE="root";
	static const String RECORD_TYPE="record";
	static const String CLASS_TYPE="class";
	static const String INTERFACE_TYPE="interface";
	static const String BEFORE_STRUCT_SEGMENT="before-struct-segment";
	static const String STRUCT_TYPE="struct";
	static const String WHITESPACE_TYPE="whitespace";
	static const String STRUCT_AFTER_CHILDREN="struct-after-children";
	static const String BLOCK_AFTER_CHILDREN="block-after-children";
	static const String BLOCK="block";
	static const String CONTENT_BEFORE_CHILD="before-child";
	static const String PARENT="caller";
	static const String GENERIC_CHILDREN="children";
	static const String FUNCTIONAL_TYPE="functional";
	static const String METHOD_CHILD="child";
	static const String DEFINITION_ANNOTATIONS="annotations";
	static const String DEFINITION_MODIFIERS="modifiers";
	static const String METHOD_TYPE="method";
	static const String INITIALIZATION_TYPE="initialization";
	static const String METHOD_DEFINITION=INITIALIZATION_TYPE;
	static const String INITIALIZATION_VALUE="value";
	static const String INITIALIZATION_DEFINITION="definition";
	static const String DEFINITION_TYPE="definition";
	static const String TUPLE_TYPE="tuple";
	static const String TUPLE_CHILDREN="children";
	static const String CONTENT_AFTER_CHILD="after-child";
	static Rule createNamespacedRule(String type, String prefix){
		const auto namespace=new StringRule("namespace");
		const auto childRule=new PrefixRule(prefix, new SuffixRule(namespace, ";"));
		return new TypeRule(type, new StripRule(childRule));
	}
	static Rule createCompoundRule(String type, String infix, Rule segmentRule){
		const auto modifiers=createModifiersRule();
		const auto maybeModifiers=new OrRule(List.of(new SuffixRule(modifiers, " "), new ExactRule("")));
		const auto nameAndContent=new InfixRule(new StringRule("name"), new FirstLocator("{"), new StripRule(new SuffixRule(new StripRule(createContentRule(segmentRule), "", STRUCT_AFTER_CHILDREN), "}")));
		const auto infixRule=new InfixRule(maybeModifiers, new FirstLocator(infix), nameAndContent);
		return new TypeRule(type, infixRule);
	}
	static Rule createStructSegmentRule(){
		const auto function=new LazyRule();
		const auto statement=createStatementRule(function);
		function.set(createMethodRule(statement));
		return new StripRule(new OrRule(List.of(function, createInitializationRule(createValueRule(statement, function)), createDefinitionStatementRule(), createWhitespaceRule())), BEFORE_STRUCT_SEGMENT, "");
	}
	static SuffixRule createDefinitionStatementRule(){
		return new SuffixRule(createDefinitionRule(), ";");
	}
	static Rule createInitializationRule(Rule value){
		const auto definition=new NodeRule(INITIALIZATION_DEFINITION, createDefinitionRule());
		const auto valueRule=new NodeRule(INITIALIZATION_VALUE, value);
		const auto infixRule=new InfixRule(definition, new FirstLocator("="), new StripRule(new SuffixRule(valueRule, ";")));
		return new TypeRule(INITIALIZATION_TYPE, infixRule);
	}
	static Rule createMethodRule(Rule statement){
		const auto orRule=new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new ExactRule(";")));
		const auto definition=createDefinitionRule();
		const auto definitionProperty=new NodeRule(METHOD_DEFINITION, definition);
		const auto params=new OptionalNodeListRule("params", new DivideRule("params", VALUE_DIVIDER, definition), new ExactRule(""));
		const auto infixRule=new InfixRule(definitionProperty, new FirstLocator("("), new InfixRule(params, new FirstLocator(")"), orRule));
		return new TypeRule(METHOD_TYPE, infixRule);
	}
	static TypeRule createBlockRule(Rule statement){
		return new TypeRule(BLOCK, new StripRule(new PrefixRule("{", new SuffixRule(new StripRule(createContentRule(statement), "", BLOCK_AFTER_CHILDREN), "}"))));
	}
	static Rule createContentRule(Rule rule){
		return new OptionalNodeListRule("children", new DivideRule("children", STATEMENT_DIVIDER, new StripRule(rule, CONTENT_BEFORE_CHILD, CONTENT_AFTER_CHILD)), new ExactRule(""));
	}
	static Rule createStatementRule(Rule function){
		const auto statement=new LazyRule();
		const auto valueRule=createValueRule(statement, function);
		statement.set(new OrRule(List.of(createKeywordRule("continue"), createKeywordRule("break"), createInitializationRule(createValueRule(statement, function)), createDefinitionStatementRule(), createConditionalRule(statement, "if", createValueRule(statement, function)), createConditionalRule(statement, "while", createValueRule(statement, function)), createElseRule(statement), createInvocationStatementRule(valueRule), createReturnRule(valueRule), createAssignmentRule(valueRule), createPostfixRule("post-increment", "++", valueRule), createPostfixRule("post-decrement", "--", valueRule), createWhitespaceRule())));
		return statement;
	}
	static TypeRule createKeywordRule(String keyword){
		return new TypeRule(keyword, new StripRule(new ExactRule(keyword+";")));
	}
	static TypeRule createElseRule(LazyRule statement){
		return new TypeRule("else", new StripRule(new PrefixRule("else ", new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new NodeRule(INITIALIZATION_VALUE, statement))))));
	}
	static TypeRule createConditionalRule(LazyRule statement, String type, Rule value){
		const auto leftRule=new StripRule(new PrefixRule("(", new NodeRule("condition", value)));
		const auto blockRule=new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new NodeRule(METHOD_CHILD, statement)));
		return new TypeRule(type, new PrefixRule(type, new InfixRule(leftRule, new ParenthesesMatcher(), blockRule)));
	}
	static TypeRule createWhitespaceRule(){
		return new TypeRule(WHITESPACE_TYPE, new StripRule(new ExactRule("")));
	}
	static Rule createPostfixRule(String type, String operator, Rule value){
		return new TypeRule(type, new SuffixRule(new NodeRule(INITIALIZATION_VALUE, value), operator+";"));
	}
	static Rule createAssignmentRule(Rule value){
		const auto destination=new NodeRule("destination", value);
		const auto source=new NodeRule("source", value);
		return new TypeRule("assignment", new SuffixRule(new InfixRule(destination, new FirstLocator("="), source), ";"));
	}
	static Rule createInvocationStatementRule(Rule value){
		return new SuffixRule(createInvocationRule(value), ";");
	}
	static TypeRule createInvocationRule(Rule value){
		const auto caller=new NodeRule("caller", value);
		const auto children=new OrRule(List.of(new DivideRule(GENERIC_CHILDREN, VALUE_DIVIDER, value), new ExactRule("")));
		const auto suffixRule=new StripRule(new SuffixRule(new InfixRule(caller, new InvocationLocator(), children), ")"));
		return new TypeRule("invocation", suffixRule);
	}
	static Rule createReturnRule(Rule value){
		return new TypeRule("return", new StripRule(new PrefixRule("return ", new SuffixRule(new NodeRule(INITIALIZATION_VALUE, value), ";"))));
	}
	static Rule createValueRule(Rule statement, Rule function){
		const auto value=new LazyRule();
		value.set(new OrRule(List.of(function, createConstructionRule(value), createInvocationRule(value), createAccessRule("data-access", ".", value), createAccessRule("method-access", "::", value), createSymbolRule(), createNumberRule(), createNotRule(value), createOperatorRule("greater-equals", ">=", value), createOperatorRule("less", "<", value), createOperatorRule("equals", "==", value), createOperatorRule("and", "&&", value), createOperatorRule("add", "+", value), createCharRule(), createStringRule(), createTernaryRule(value), createLambdaRule(statement, value))));
		return value;
	}
	static TypeRule createLambdaRule(Rule statement, LazyRule value){
		return new TypeRule("lambda", new InfixRule(new StringRule("args"), new FirstLocator("->"), new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new NodeRule(METHOD_CHILD, value)))));
	}
	static TypeRule createStringRule(){
		const auto value=new PrefixRule("\"", new SuffixRule(new StringRule(INITIALIZATION_VALUE), "\""));
		return new TypeRule("string", new StripRule(value));
	}
	static TypeRule createTernaryRule(LazyRule value){
		return new TypeRule("ternary", new InfixRule(new NodeRule("condition", value), new FirstLocator("?"), new InfixRule(new NodeRule("ifTrue", value), new FirstLocator(":"), new NodeRule("ifElse", value))));
	}
	static TypeRule createCharRule(){
		return new TypeRule("char", new StripRule(new PrefixRule("'", new SuffixRule(new StringRule(INITIALIZATION_VALUE), "'"))));
	}
	static TypeRule createNumberRule(){
		return new TypeRule("number", new StripRule(new FilterRule(new NumberFilter(), new StringRule(INITIALIZATION_VALUE))));
	}
	static TypeRule createOperatorRule(String type, String operator, LazyRule value){
		return new TypeRule(type, new InfixRule(new NodeRule("left", value), new FirstLocator(operator), new NodeRule("right", value)));
	}
	static TypeRule createNotRule(LazyRule value){
		return new TypeRule("not", new StripRule(new PrefixRule("!", new NodeRule(INITIALIZATION_VALUE, value))));
	}
	static TypeRule createConstructionRule(LazyRule value){
		const auto type=new StringRule("type");
		const auto arguments=new OrRule(List.of(new DivideRule("arguments", VALUE_DIVIDER, value), new ExactRule("")));
		const auto childRule=new InfixRule(type, new FirstLocator("("), new StripRule(new SuffixRule(arguments, ")")));
		return new TypeRule("construction", new StripRule(new PrefixRule("new ", childRule)));
	}
	static Rule createSymbolRule(){
		return new TypeRule("symbol", new StripRule(new FilterRule(new SymbolFilter(), new StringRule("value"))));
	}
	static Rule createAccessRule(String type, String infix, const Rule value){
		const auto rule=new InfixRule(new NodeRule("ref", value), new LastLocator(infix), new StringRule("property"));
		return new TypeRule(type, rule);
	}
	static Rule createDefinitionRule(){
		const auto name=new FilterRule(new SymbolFilter(), new StringRule("name"));
		const auto typeProperty=new NodeRule("type", createTypeRule());
		const auto typeAndName=new StripRule(new InfixRule(typeProperty, new LastLocator(" "), name));
		const auto modifiers=createModifiersRule();
		const auto maybeModifiers=new OrRule(List.of(modifiers, new ExactRule("")));
		const auto typeParams=new StringRule("type-params");
		const auto maybeTypeParams=new OrRule(List.of(new ContextRule("With type params", new InfixRule(new StripRule(new PrefixRule("<", typeParams)), new FirstLocator(">"), new StripRule(typeAndName))), new ContextRule("Without type params", typeAndName)));
		const auto withModifiers=new OptionalNodeListRule("modifiers", new ContextRule("With modifiers", new StripRule(new InfixRule(maybeModifiers, new BackwardsLocator(" "), maybeTypeParams))), new ContextRule("Without modifiers", maybeTypeParams));
		const auto annotation=new TypeRule("annotation", new StripRule(new PrefixRule("@", new StringRule(INITIALIZATION_VALUE))));
		const auto annotations=new DivideRule(DEFINITION_ANNOTATIONS, new SimpleDivider("\n"), annotation);
		return new TypeRule(DEFINITION_TYPE, new OrRule(List.of(new ContextRule("With annotations", new InfixRule(annotations, new LastLocator("\n"), withModifiers)), new ContextRule("Without annotations", withModifiers))));
	}
	static DivideRule createModifiersRule(){
		const auto modifierRule=new TypeRule("modifier", new StripRule(new FilterRule(new SymbolFilter(), new StringRule(INITIALIZATION_VALUE))));
		return new DivideRule("modifiers", new SimpleDivider(" "), modifierRule);
	}
	static Rule createTypeRule(){
		const auto type=new LazyRule();
		type.set(new OrRule(List.of(createSymbolRule(), createGenericRule(type), createVarArgsRule(type), createArrayRule(type), createFunctionalRule(type), createTupleRule(type), createSliceRule(type))));
		return type;
	}
	static TypeRule createSliceRule(LazyRule type){
		return new TypeRule("slice", new PrefixRule("&[", new SuffixRule(new NodeRule("child", type), "]")));
	}
	static TypeRule createTupleRule(LazyRule type){
		return new TypeRule(TUPLE_TYPE, new PrefixRule("[", new SuffixRule(new DivideRule(TUPLE_CHILDREN, VALUE_DIVIDER, type), "]")));
	}
	static TypeRule createFunctionalRule(Rule type){
		const auto params=new OptionalNodeListRule("params", new DivideRule("params", VALUE_DIVIDER, type), new ExactRule(""));
		const auto leftRule=new PrefixRule("(", new SuffixRule(params, ")"));
		const auto rule=new InfixRule(leftRule, new FirstLocator(" => "), new NodeRule("return", type));
		return new TypeRule(FUNCTIONAL_TYPE, new PrefixRule("(", new SuffixRule(rule, ")")));
	}
	static TypeRule createArrayRule(LazyRule type){
		return new TypeRule("array", new SuffixRule(new NodeRule(METHOD_CHILD, type), "[]"));
	}
	static TypeRule createVarArgsRule(LazyRule type){
		return new TypeRule("var-args", new SuffixRule(new NodeRule(METHOD_CHILD, type), "..."));
	}
	static TypeRule createGenericRule(LazyRule type){
		return new TypeRule("generic", new InfixRule(new StripRule(new StringRule(PARENT)), new FirstLocator("<"), new SuffixRule(new DivideRule(GENERIC_CHILDREN, VALUE_DIVIDER, type), ">")));
	}
}

