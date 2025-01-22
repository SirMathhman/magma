import magma.app.locate.BackwardsLocator;import magma.app.locate.InvocationLocator;import magma.app.rule.ContextRule;import magma.app.rule.ExactRule;import magma.app.rule.FilterRule;import magma.app.rule.InfixRule;import magma.app.rule.LazyRule;import magma.app.rule.NodeRule;import magma.app.rule.OptionalNodeListRule;import magma.app.rule.OptionalNodeRule;import magma.app.rule.OrRule;import magma.app.rule.PrefixRule;import magma.app.rule.Rule;import magma.app.rule.StringRule;import magma.app.rule.StripRule;import magma.app.rule.SuffixRule;import magma.app.rule.TypeRule;import magma.app.rule.divide.DivideRule;import magma.app.rule.divide.SimpleDivider;import magma.app.rule.filter.NumberFilter;import magma.app.rule.filter.SymbolFilter;import magma.app.rule.locate.FirstLocator;import magma.app.rule.locate.LastLocator;import magma.app.rule.locate.ParenthesesMatcher;import java.util.List;import static magma.app.rule.divide.StatementDivider.STATEMENT_DIVIDER;import static magma.app.rule.divide.ValueDivider.VALUE_DIVIDER;struct CommonLang {String ROOT_TYPE="root";String RECORD_TYPE="record";String CLASS_TYPE="class";String INTERFACE_TYPE="interface";String BEFORE_STRUCT_SEGMENT="before-struct-segment";String STRUCT_TYPE="struct";String WHITESPACE_TYPE="whitespace";String STRUCT_AFTER_CHILDREN="struct-after-children";String BLOCK_AFTER_CHILDREN="block-after-children";String BLOCK="block";String CONTENT_BEFORE_CHILD="before-child";String PARENT="caller";String GENERIC_CHILDREN="children";String FUNCTIONAL_TYPE="functional";String METHOD_CHILD="child";String DEFINITION_ANNOTATIONS="annotations";String METHOD_TYPE="method";String INITIALIZATION_TYPE="initialization";String METHOD_DEFINITION="definition";String INITIALIZATION_VALUE="value";String INITIALIZATION_DEFINITION="definition";String DEFINITION_TYPE="definition";String TUPLE_TYPE="tuple";String TUPLE_CHILDREN="children";String CONTENT_AFTER_CHILD="after-child";Rule createNamespacedRule(String type, String prefix){
	var namespace=new StringRule("namespace");
	var childRule=new PrefixRule(prefix, new SuffixRule(namespace, ";"));
	return new TypeRule(type, new StripRule(childRule));}Rule createCompoundRule(String type, String infix, Rule segmentRule){
	var modifiers=createModifiersRule();
	var maybeModifiers=new OptionalNodeRule("modifiers", new SuffixRule(modifiers, " "));
	var nameAndContent=new InfixRule(new StringRule("name"), new FirstLocator("{"), new StripRule(new SuffixRule(new StripRule(createContentRule(segmentRule), "", STRUCT_AFTER_CHILDREN), "}")));
	var infixRule=new InfixRule(maybeModifiers, new FirstLocator(infix), nameAndContent);
	return new TypeRule(type, infixRule);}StripRule createStructSegmentRule(LazyRule function, Rule statement){
	return new StripRule(new OrRule(List.of(function, createInitializationRule(createValueRule(statement, function)), createDefinitionStatementRule(), createWhitespaceRule())), BEFORE_STRUCT_SEGMENT, "");}SuffixRule createDefinitionStatementRule(){
	return new SuffixRule(createDefinitionRule(), ";");}Rule createInitializationRule(Rule value){
	var definition=new NodeRule(INITIALIZATION_DEFINITION, createDefinitionRule());
	var valueRule=new NodeRule(INITIALIZATION_VALUE, value);
	var infixRule=new InfixRule(definition, new FirstLocator("="), new StripRule(new SuffixRule(valueRule, ";")));
	return new TypeRule(INITIALIZATION_TYPE, infixRule);}Rule createMethodRule(Rule statement){
	var orRule=new OptionalNodeRule(METHOD_CHILD, new NodeRule(METHOD_CHILD, createBlockRule(statement)), new ExactRule(";"));
	var definition=createDefinitionRule();
	var definitionProperty=new NodeRule(METHOD_DEFINITION, definition);
	var params=new OptionalNodeListRule("params", new DivideRule("params", VALUE_DIVIDER, definition));
	var infixRule=new InfixRule(definitionProperty, new FirstLocator("("), new InfixRule(params, new FirstLocator(")"), orRule));
	return new TypeRule(METHOD_TYPE, infixRule);}TypeRule createBlockRule(Rule statement){
	return new TypeRule(BLOCK, new StripRule(new PrefixRule("{", new SuffixRule(new StripRule(createContentRule(statement), "", BLOCK_AFTER_CHILDREN), "}"))));}Rule createContentRule(Rule rule){
	return new OptionalNodeListRule("children", new DivideRule("children", STATEMENT_DIVIDER, new StripRule(rule, CONTENT_BEFORE_CHILD, CONTENT_AFTER_CHILD)));}Rule createStatementRule(Rule function){
	var statement=new LazyRule();
	var valueRule=createValueRule(statement, function);
	statement.set(new OrRule(List.of(createKeywordRule("continue"), createKeywordRule("break"), createInitializationRule(createValueRule(statement, function)), createDefinitionStatementRule(), createConditionalRule(statement, "if", createValueRule(statement, function)), createConditionalRule(statement, "while", createValueRule(statement, function)), createElseRule(statement), createInvocationStatementRule(valueRule), createReturnRule(valueRule), createAssignmentRule(valueRule), createPostfixRule("post-increment", "++", valueRule), createPostfixRule("post-decrement", "--", valueRule), createWhitespaceRule())));
	return statement;}TypeRule createKeywordRule(String keyword){
	return new TypeRule(keyword, new StripRule(new ExactRule(keyword+";")));}TypeRule createElseRule(LazyRule statement){
	return new TypeRule("else", new StripRule(new PrefixRule("else ", new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new NodeRule(INITIALIZATION_VALUE, statement))))));}TypeRule createConditionalRule(LazyRule statement, String type, Rule value){
	var leftRule=new StripRule(new PrefixRule("(", new NodeRule("condition", value)));
	var blockRule=new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new NodeRule(METHOD_CHILD, statement)));
	return new TypeRule(type, new PrefixRule(type, new InfixRule(leftRule, new ParenthesesMatcher(), blockRule)));}TypeRule createWhitespaceRule(){
	return new TypeRule(WHITESPACE_TYPE, new StripRule(new ExactRule("")));}Rule createPostfixRule(String type, String operator, Rule value){
	return new TypeRule(type, new SuffixRule(new NodeRule(INITIALIZATION_VALUE, value), operator+";"));}Rule createAssignmentRule(Rule value){
	var destination=new NodeRule("destination", value);
	var source=new NodeRule("source", value);
	return new TypeRule("assignment", new SuffixRule(new InfixRule(destination, new FirstLocator("="), source), ";"));}Rule createInvocationStatementRule(Rule value){
	return new SuffixRule(createInvocationRule(value), ";");}TypeRule createInvocationRule(Rule value){
	var caller=new NodeRule("caller", value);
	var children=new OptionalNodeListRule("children", new DivideRule("children", VALUE_DIVIDER, value));
	var suffixRule=new StripRule(new SuffixRule(new InfixRule(caller, new InvocationLocator(), children), ")"));
	return new TypeRule("invocation", suffixRule);}Rule createReturnRule(Rule value){
	return new TypeRule("return", new StripRule(new PrefixRule("return ", new SuffixRule(new NodeRule(INITIALIZATION_VALUE, value), ";"))));}Rule createValueRule(Rule statement, Rule function){
	var value=new LazyRule();
	value.set(new OrRule(List.of(createLambdaRule(statement, value), function, createConstructionRule(value), createInvocationRule(value), createAccessRule("data-access", ".", value), createAccessRule("method-access", "::", value), createSymbolRule(), createNumberRule(), createNotRule(value), createOperatorRule("greater-equals", ">=", value), createOperatorRule("less", "<", value), createOperatorRule("equals", "==", value), createOperatorRule("and", "&&", value), createOperatorRule("add", "+", value), createCharRule(), createStringRule(), createTernaryRule(value))));
	return value;}TypeRule createLambdaRule(Rule statement, LazyRule value){
	var args=new StripRule(new OrRule(List.of(new ExactRule("()"), new NodeRule("arg", createSymbolRule()), new DivideRule("args", new SimpleDivider(","), createSymbolRule()))));
	return new TypeRule("lambda", new InfixRule(args, new FirstLocator("->"), new OrRule(List.of(new NodeRule(METHOD_CHILD, createBlockRule(statement)), new NodeRule(METHOD_CHILD, value)))));}TypeRule createStringRule(){
	var value=new PrefixRule("\"", new SuffixRule(new StringRule(INITIALIZATION_VALUE), "\""));
	return new TypeRule("string", new StripRule(value));}TypeRule createTernaryRule(LazyRule value){
	return new TypeRule("ternary", new InfixRule(new NodeRule("condition", value), new FirstLocator("?"), new InfixRule(new NodeRule("ifTrue", value), new FirstLocator(":"), new NodeRule("ifElse", value))));}TypeRule createCharRule(){
	return new TypeRule("char", new StripRule(new PrefixRule("'", new SuffixRule(new StringRule(INITIALIZATION_VALUE), "'"))));}TypeRule createNumberRule(){
	return new TypeRule("number", new StripRule(new FilterRule(new NumberFilter(), new StringRule(INITIALIZATION_VALUE))));}TypeRule createOperatorRule(String type, String operator, LazyRule value){
	return new TypeRule(type, new InfixRule(new NodeRule("left", value), new FirstLocator(operator), new NodeRule("right", value)));}TypeRule createNotRule(LazyRule value){
	return new TypeRule("not", new StripRule(new PrefixRule("!", new NodeRule(INITIALIZATION_VALUE, value))));}TypeRule createConstructionRule(LazyRule value){
	var type=new StringRule("type");
	var arguments=new OptionalNodeListRule("arguments", new DivideRule("arguments", VALUE_DIVIDER, value));
	var childRule=new InfixRule(type, new FirstLocator("("), new StripRule(new SuffixRule(arguments, ")")));
	return new TypeRule("construction", new StripRule(new PrefixRule("new ", childRule)));}Rule createSymbolRule(){
	return new TypeRule("symbol", new StripRule(new FilterRule(new SymbolFilter(), new StringRule("value"))));}Rule createAccessRule(String type, String infix, Rule value){
	var rule=new InfixRule(new NodeRule("ref", value), new LastLocator(infix), new StringRule("property"));
	return new TypeRule(type, rule);}Rule createDefinitionRule(){
	var name=new FilterRule(new SymbolFilter(), new StringRule("name"));
	var typeProperty=new NodeRule("type", createTypeRule());
	var typeAndName=new StripRule(new InfixRule(typeProperty, new LastLocator(" "), name));
	var modifiers=createModifiersRule();
	var typeParams=new StringRule("type-params");
	var maybeTypeParams=new OrRule(List.of(new ContextRule("With type params", new InfixRule(new StripRule(new PrefixRule("<", typeParams)), new FirstLocator(">"), new StripRule(typeAndName))), new ContextRule("Without type params", typeAndName)));
	var withModifiers=new OptionalNodeListRule("modifiers", new ContextRule("With modifiers", new StripRule(new InfixRule(modifiers, new BackwardsLocator(" "), maybeTypeParams))), new ContextRule("Without modifiers", maybeTypeParams));
	var annotation=new TypeRule("annotation", new StripRule(new PrefixRule("@", new StringRule(INITIALIZATION_VALUE))));
	var annotations=new DivideRule(DEFINITION_ANNOTATIONS, new SimpleDivider("\n"), annotation);
	return new TypeRule(DEFINITION_TYPE, new OrRule(List.of(new ContextRule("With annotations", new InfixRule(annotations, new LastLocator("\n"), withModifiers)), new ContextRule("Without annotations", withModifiers))));}DivideRule createModifiersRule(){
	var modifierRule=new TypeRule("modifier", new StripRule(new FilterRule(new SymbolFilter(), new StringRule(INITIALIZATION_VALUE))));
	return new DivideRule("modifiers", new SimpleDivider(" "), modifierRule);}Rule createTypeRule(){
	var type=new LazyRule();
	type.set(new OrRule(List.of(createSymbolRule(), createGenericRule(type), createVarArgsRule(type), createArrayRule(type), createFunctionalRule(type), createTupleRule(type), createSliceRule(type))));
	return type;}TypeRule createSliceRule(LazyRule type){
	return new TypeRule("slice", new PrefixRule("&[", new SuffixRule(new NodeRule("child", type), "]")));}TypeRule createTupleRule(LazyRule type){
	return new TypeRule(TUPLE_TYPE, new PrefixRule("[", new SuffixRule(new DivideRule(TUPLE_CHILDREN, VALUE_DIVIDER, type), "]")));}TypeRule createFunctionalRule(Rule type){
	var params=new OptionalNodeListRule("params", new DivideRule("params", VALUE_DIVIDER, type));
	var leftRule=new PrefixRule("(", new SuffixRule(params, ")"));
	var rule=new InfixRule(leftRule, new FirstLocator(" => "), new NodeRule("return", type));
	return new TypeRule(FUNCTIONAL_TYPE, new PrefixRule("(", new SuffixRule(rule, ")")));}TypeRule createArrayRule(LazyRule type){
	return new TypeRule("array", new SuffixRule(new NodeRule(METHOD_CHILD, type), "[]"));}TypeRule createVarArgsRule(LazyRule type){
	return new TypeRule("var-args", new SuffixRule(new NodeRule(METHOD_CHILD, type), "..."));}TypeRule createGenericRule(LazyRule type){
	return new TypeRule("generic", new InfixRule(new StripRule(new StringRule(PARENT)), new FirstLocator("<"), new SuffixRule(new DivideRule(GENERIC_CHILDREN, VALUE_DIVIDER, type), ">")));}}