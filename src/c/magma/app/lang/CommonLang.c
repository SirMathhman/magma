import magma.app.locate.BackwardsLocator;import magma.app.locate.InvocationLocator;import magma.app.rule.ContextRule;import magma.app.rule.ExactRule;import magma.app.rule.FilterRule;import magma.app.rule.InfixRule;import magma.app.rule.LazyRule;import magma.app.rule.NodeRule;import magma.app.rule.OptionalNodeListRule;import magma.app.rule.OptionalNodeRule;import magma.app.rule.OrRule;import magma.app.rule.PrefixRule;import magma.app.rule.Rule;import magma.app.rule.StringRule;import magma.app.rule.StripRule;import magma.app.rule.SuffixRule;import magma.app.rule.TypeRule;import magma.app.rule.divide.DivideRule;import magma.app.rule.divide.SimpleDivider;import magma.app.rule.filter.NumberFilter;import magma.app.rule.filter.SymbolFilter;import magma.app.rule.locate.FirstLocator;import magma.app.rule.locate.LastLocator;import magma.app.rule.locate.ParenthesesMatcher;import java.util.List;import static magma.app.rule.divide.StatementDivider.STATEMENT_DIVIDER;import static magma.app.rule.divide.ValueDivider.VALUE_DIVIDER;struct CommonLang{
	String ROOT_TYPE="root";
	String RECORD_TYPE="record";
	String CLASS_TYPE="class";
	String INTERFACE_TYPE="interface";
	String BEFORE_STRUCT_SEGMENT="before-struct-segment";
	String STRUCT_TYPE="struct";
	String WHITESPACE_TYPE="whitespace";
	String CONTENT_BEFORE_CHILD="content-before-child";
	String GENERIC_PARENT="caller";
	String GENERIC_CHILDREN="generic-children";
	String FUNCTIONAL_TYPE="functional";
	String METHOD_VALUE="method-child";
	String DEFINITION_ANNOTATIONS="annotations";
	String METHOD_TYPE="method";
	String INITIALIZATION_TYPE="initialization";
	String METHOD_DEFINITION="definition";
	String INITIALIZATION_VALUE="value";
	String INITIALIZATION_DEFINITION="definition";
	String DEFINITION_TYPE="definition";
	String TUPLE_TYPE="tuple";
	String TUPLE_CHILDREN="tuple-children";
	String CONTENT_AFTER_CHILD="after-child";
	String CONTENT_CHILDREN="content-children";
	String INVOCATION_CHILDREN="invocation-children";
	String CONTENT_AFTER_CHILDREN="content-after-children";
	String GENERIC_TYPE="generic";
	String METHOD_PARAMS="params";
	String FUNCTIONAL_PARAMS="params";
	String FUNCTIONAL_RETURN="return";
	Rule createNamespacedRule(String type, String prefix){
		var namespace=new StringRule("namespace");
		var childRule=new PrefixRule(prefix, new SuffixRule(namespace, ";"));
		return new TypeRule(type, new StripRule(childRule));
	}
	Rule createCompoundRule(String type, String infix, Rule segmentRule){
		var modifiers=createModifiersRule();
		var maybeModifiers=new OptionalNodeRule("modifiers", new SuffixRule(modifiers, " "));
		var name=new StripRule(new FilterRule(new SymbolFilter(), new StringRule("name")));
		var typeParams=new DivideRule("type-params", VALUE_DIVIDER, createTypeRule());
		var maybeTypeParams=new OptionalNodeListRule("type-params", new InfixRule(name, new FirstLocator("<"), new StripRule(new SuffixRule(typeParams, ">"))), name);
		var params=new DivideRule("params", VALUE_DIVIDER, createDefinitionRule());
		var maybeParams=new OptionalNodeListRule("params", new InfixRule(maybeTypeParams, new FirstLocator("("), new StripRule(new SuffixRule(params, ")"))), maybeTypeParams);
		var supertype=new NodeRule("supertype", createTypeRule());
		var maybeImplements=new OptionalNodeRule("supertype", new InfixRule(maybeParams, new FirstLocator(" implements "), supertype), maybeParams);
		var nameAndContent=wrapUsingBlock("value", maybeImplements, segmentRule);
		var infixRule=new InfixRule(maybeModifiers, new FirstLocator(infix), nameAndContent);
		return new TypeRule(type, infixRule);
	}
	StripRule createStructSegmentRule(LazyRule function, Rule statement, LazyRule struct){
		return new StripRule(new OrRule(List.of(function, createInitializationRule(createValueRule(statement, function)), createDefinitionStatementRule(), createWhitespaceRule(), struct)), BEFORE_STRUCT_SEGMENT, "");
	}
	SuffixRule createDefinitionStatementRule(){
		return new SuffixRule(createDefinitionRule(), ";");
	}
	Rule createInitializationRule(Rule value){
		var definition=new NodeRule(INITIALIZATION_DEFINITION, createDefinitionRule());
		var valueRule=new NodeRule(INITIALIZATION_VALUE, value);
		var infixRule=new InfixRule(definition, new FirstLocator("="), new StripRule(new SuffixRule(valueRule, ";")));
		return new TypeRule(INITIALIZATION_TYPE, infixRule);
	}
	Rule createMethodRule(Rule statement){
		var definition=createDefinitionRule();
		var definitionProperty=new NodeRule(METHOD_DEFINITION, definition);
		var params=new OptionalNodeListRule(METHOD_PARAMS, new DivideRule(METHOD_PARAMS, VALUE_DIVIDER, definition));
		var infixRule=new InfixRule(definitionProperty, new FirstLocator("("), new StripRule(new SuffixRule(params, ")")));
		var orRule=new OptionalNodeRule(METHOD_VALUE, new ContextRule("With block", wrapUsingBlock(METHOD_VALUE, infixRule, statement)), new ContextRule("With statement", new StripRule(new SuffixRule(infixRule, ";"))));
		return new TypeRule(METHOD_TYPE, orRule);
	}
	Rule wrapUsingBlock(String propertyKey, Rule beforeBlock, Rule statement){
		var withEnd=new NodeRule(propertyKey, new TypeRule("block", createContentRule(statement)));
		return new StripRule(new InfixRule(beforeBlock, new FirstLocator("{"), new SuffixRule(withEnd, "}")));
	}
	Rule createContentRule(Rule rule){
		return new StripRule(new OptionalNodeListRule(CONTENT_CHILDREN, new DivideRule(CONTENT_CHILDREN, STATEMENT_DIVIDER, new StripRule(rule, CONTENT_BEFORE_CHILD, CONTENT_AFTER_CHILD))), "", CONTENT_AFTER_CHILDREN);
	}
	Rule createStatementRule(Rule function, LazyRule struct){
		var statement=new LazyRule();
		var valueRule=createValueRule(statement, function);
		statement.set(new OrRule(List.of(createKeywordRule("continue"), createKeywordRule("break"), createInitializationRule(createValueRule(statement, function)), createDefinitionStatementRule(), createConditionalRule(statement, "if", createValueRule(statement, function)), createConditionalRule(statement, "while", createValueRule(statement, function)), createElseRule(statement), createInvocationStatementRule(valueRule), createReturnRule(valueRule), createAssignmentRule(valueRule), createPostfixRule("post-increment", "++", valueRule), createPostfixRule("post-decrement", "--", valueRule), createWhitespaceRule())));
		return statement;
	}
	TypeRule createKeywordRule(String keyword){
		return new TypeRule(keyword, new StripRule(new ExactRule(keyword+";")));
	}
	TypeRule createElseRule(LazyRule statement){
		return new TypeRule("else", new OrRule(List.of(wrapUsingBlock("value", new StripRule(new ExactRule("else")), statement), new PrefixRule("else ", new NodeRule("value", statement)))));
	}
	TypeRule createConditionalRule(LazyRule statement, String type, Rule value){
		var condition=new NodeRule("condition", value);
		return new TypeRule(type, new StripRule(new PrefixRule(type, new OrRule(List.of(
                new ContextRule("With block", wrapUsingBlock("value", new StripRule(new PrefixRule("(", new SuffixRule(condition, ")"))), statement)),
                new ContextRule("With statement", new StripRule(new PrefixRule("(", new InfixRule(condition, new ParenthesesMatcher(), new NodeRule("value", statement)))))
        )))));
	}
	TypeRule createWhitespaceRule(){
		return new TypeRule(WHITESPACE_TYPE, new StripRule(new ExactRule("")));
	}
	Rule createPostfixRule(String type, String operator, Rule value){
		return new TypeRule(type, new SuffixRule(new NodeRule(INITIALIZATION_VALUE, value), operator+";"));
	}
	Rule createAssignmentRule(Rule value){
		var destination=new NodeRule("destination", value);
		var source=new NodeRule("source", value);
		return new TypeRule("assignment", new SuffixRule(new InfixRule(destination, new FirstLocator("="), source), ";"));
	}
	Rule createInvocationStatementRule(Rule value){
		return new SuffixRule(createInvocationRule(value), ";");
	}
	TypeRule createInvocationRule(Rule value){
		var caller=new NodeRule("caller", value);
		var children=new OptionalNodeListRule(INVOCATION_CHILDREN, new DivideRule(INVOCATION_CHILDREN, VALUE_DIVIDER, value));
		var suffixRule=new StripRule(new SuffixRule(new InfixRule(caller, new InvocationLocator(), children), ")"));
		return new TypeRule("invocation", suffixRule);
	}
	Rule createReturnRule(Rule value){
		return new TypeRule("return", new StripRule(new PrefixRule("return ", new SuffixRule(new NodeRule(INITIALIZATION_VALUE, value), ";"))));
	}
	Rule createValueRule(Rule statement, Rule function){
		var value=new LazyRule();
		value.set(new OrRule(List.of(createLambdaRule(statement, value), function, createConstructionRule(value), createInvocationRule(value), createAccessRule("data-access", ".", value), createAccessRule("method-access", "::", value), createSymbolRule(), createNumberRule(), createNotRule(value), createOperatorRule("greater-equals", ">=", value), createOperatorRule("less", "<", value), createOperatorRule("equals", "==", value), createOperatorRule("and", "&&", value), createOperatorRule("add", "+", value), createCharRule(), createStringRule(), createTernaryRule(value))));
		return value;
	}
	TypeRule createLambdaRule(Rule statement, LazyRule value){
		var args=new StripRule(new OrRule(List.of(new ExactRule("()"), new NodeRule("arg", createSymbolRule()), new DivideRule("args", new SimpleDivider(","), createSymbolRule()))));
		var rightRule=new OrRule(List.of(new NodeRule("value", wrapUsingBlock("value", new StripRule(new SuffixRule(args, "->")), statement)), new InfixRule(args, new FirstLocator("->"), new NodeRule("value", value))));
		return new TypeRule("lambda", rightRule);
	}
	TypeRule createStringRule(){
		var value=new PrefixRule("\"", new SuffixRule(new StringRule(INITIALIZATION_VALUE), "\""));
		return new TypeRule("string", new StripRule(value));
	}
	TypeRule createTernaryRule(LazyRule value){
		return new TypeRule("ternary", new InfixRule(new NodeRule("condition", value), new FirstLocator("?"), new InfixRule(new NodeRule("ifTrue", value), new FirstLocator(":"), new NodeRule("ifElse", value))));
	}
	TypeRule createCharRule(){
		return new TypeRule("char", new StripRule(new PrefixRule("'", new SuffixRule(new StringRule(INITIALIZATION_VALUE), "'"))));
	}
	TypeRule createNumberRule(){
		return new TypeRule("number", new StripRule(new FilterRule(new NumberFilter(), new StringRule(INITIALIZATION_VALUE))));
	}
	TypeRule createOperatorRule(String type, String operator, LazyRule value){
		return new TypeRule(type, new InfixRule(new NodeRule("left", value), new FirstLocator(operator), new NodeRule("right", value)));
	}
	TypeRule createNotRule(LazyRule value){
		return new TypeRule("not", new StripRule(new PrefixRule("!", new NodeRule(INITIALIZATION_VALUE, value))));
	}
	TypeRule createConstructionRule(LazyRule value){
		var type=new StringRule("type");
		var arguments=new OptionalNodeListRule("arguments", new DivideRule("arguments", VALUE_DIVIDER, value));
		var childRule=new InfixRule(type, new FirstLocator("("), new StripRule(new SuffixRule(arguments, ")")));
		return new TypeRule("construction", new StripRule(new PrefixRule("new ", childRule)));
	}
	Rule createSymbolRule(){
		return new TypeRule("symbol", new StripRule(new FilterRule(new SymbolFilter(), new StringRule("value"))));
	}
	Rule createAccessRule(String type, String infix, Rule value){
		var rule=new InfixRule(new NodeRule("ref", value), new LastLocator(infix), new StringRule("property"));
		return new TypeRule(type, rule);
	}
	Rule createDefinitionRule(){
		var name=new FilterRule(new SymbolFilter(), new StringRule("name"));
		var typeProperty=new NodeRule("type", createTypeRule());
		var typeAndName=new StripRule(new InfixRule(typeProperty, new LastLocator(" "), name));
		var modifiers=createModifiersRule();
		var typeParams=new StringRule("type-params");
		var maybeTypeParams=new OrRule(List.of(new ContextRule("With type params", new InfixRule(new StripRule(new PrefixRule("<", typeParams)), new FirstLocator(">"), new StripRule(typeAndName))), new ContextRule("Without type params", typeAndName)));
		var withModifiers=new OptionalNodeListRule("modifiers", new ContextRule("With modifiers", new StripRule(new InfixRule(modifiers, new BackwardsLocator(" "), maybeTypeParams))), new ContextRule("Without modifiers", maybeTypeParams));
		var annotation=new TypeRule("annotation", new StripRule(new PrefixRule("@", new StringRule(INITIALIZATION_VALUE))));
		var annotations=new DivideRule(DEFINITION_ANNOTATIONS, new SimpleDivider("\n"), annotation);
		return new TypeRule(DEFINITION_TYPE, new OrRule(List.of(new ContextRule("With annotations", new InfixRule(annotations, new LastLocator("\n"), withModifiers)), new ContextRule("Without annotations", withModifiers))));
	}
	DivideRule createModifiersRule(){
		var modifierRule=new TypeRule("modifier", new StripRule(new FilterRule(new SymbolFilter(), new StringRule(INITIALIZATION_VALUE))));
		return new DivideRule("modifiers", new SimpleDivider(" "), modifierRule);
	}
	Rule createTypeRule(){
		var type=new LazyRule();
		type.set(new OrRule(List.of(createSymbolRule(), createGenericRule(type), createVarArgsRule(type), createArrayRule(type), createFunctionalRule(type), createTupleRule(type), createSliceRule(type), createStructRule())));
		return type;
	}
	TypeRule createStructRule(){
		return new TypeRule("struct", new PrefixRule("struct ", new StringRule("value")));
	}
	TypeRule createSliceRule(LazyRule type){
		return new TypeRule("slice", new PrefixRule("&[", new SuffixRule(new NodeRule("child", type), "]")));
	}
	TypeRule createTupleRule(LazyRule type){
		return new TypeRule(TUPLE_TYPE, new PrefixRule("[", new SuffixRule(new DivideRule(TUPLE_CHILDREN, VALUE_DIVIDER, type), "]")));
	}
	TypeRule createFunctionalRule(Rule type){
		var params=new OptionalNodeListRule(FUNCTIONAL_PARAMS, new DivideRule(FUNCTIONAL_PARAMS, VALUE_DIVIDER, type));
		var leftRule=new PrefixRule("(", new SuffixRule(params, ")"));
		var rule=new InfixRule(new NodeRule(FUNCTIONAL_RETURN, type), new FirstLocator(" (*)"), leftRule);
		return new TypeRule(FUNCTIONAL_TYPE, rule);
	}
	TypeRule createArrayRule(LazyRule type){
		return new TypeRule("array", new SuffixRule(new NodeRule(METHOD_VALUE, type), "[]"));
	}
	TypeRule createVarArgsRule(LazyRule type){
		return new TypeRule("var-args", new SuffixRule(new NodeRule(METHOD_VALUE, type), "..."));
	}
	TypeRule createGenericRule(LazyRule type){
		var parent=new StringRule(GENERIC_PARENT);
		var children=new DivideRule(GENERIC_CHILDREN, VALUE_DIVIDER, type);
		return new TypeRule(GENERIC_TYPE, new InfixRule(new StripRule(parent), new FirstLocator("<"), new StripRule(new SuffixRule(children, ">"))));
	}
}