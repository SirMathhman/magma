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
	Rule createNamespacedRule(String type, String prefix){
		var namespace=StringRule.new();
		var childRule=PrefixRule.new();
		return TypeRule.new();
	}
	Rule createCompoundRule(String type, String infix, Rule segmentRule){
		var modifiers=createModifiersRule();
		var maybeModifiers=OptionalNodeRule.new();
		var name=StripRule.new();
		var typeParams=DivideRule.new();
		var maybeTypeParams=OptionalNodeListRule.new();
		var params=DivideRule.new();
		var maybeParams=OptionalNodeListRule.new();
		var supertype=NodeRule.new();
		var maybeImplements=OptionalNodeRule.new();
		var nameAndContent=wrapUsingBlock("value", maybeImplements, segmentRule);
		var infixRule=InfixRule.new();
		return TypeRule.new();
	}
	StripRule createStructSegmentRule(LazyRule function, Rule statement){
		return StripRule.new();
	}
	SuffixRule createDefinitionStatementRule(){
		return SuffixRule.new();
	}
	Rule createInitializationRule(Rule value){
		var definition=NodeRule.new();
		var valueRule=NodeRule.new();
		var infixRule=InfixRule.new();
		return TypeRule.new();
	}
	Rule createMethodRule(Rule statement){
		var definition=createDefinitionRule();
		var definitionProperty=NodeRule.new();
		var params=OptionalNodeListRule.new();
		var infixRule=InfixRule.new();
		var orRule=OptionalNodeRule.new();
		return TypeRule.new();
	}
	Rule wrapUsingBlock(String propertyKey, Rule beforeBlock, Rule statement){
		var withEnd=NodeRule.new();
		return StripRule.new();
	}
	Rule createContentRule(Rule rule){
		return StripRule.new();
	}
	Rule createStatementRule(Rule function){
		var statement=LazyRule.new();
		var valueRule=createValueRule(statement, function);
		statement.set(OrRule.new());
		return statement;
	}
	TypeRule createKeywordRule(String keyword){
		return TypeRule.new();
	}
	TypeRule createElseRule(LazyRule statement){
		return TypeRule.new();
	}
	TypeRule createConditionalRule(LazyRule statement, String type, Rule value){
		var condition=NodeRule.new();
		return TypeRule.new();
	}
	TypeRule createWhitespaceRule(){
		return TypeRule.new();
	}
	Rule createPostfixRule(String type, String operator, Rule value){
		return TypeRule.new();
	}
	Rule createAssignmentRule(Rule value){
		var destination=NodeRule.new();
		var source=NodeRule.new();
		return TypeRule.new();
	}
	Rule createInvocationStatementRule(Rule value){
		return SuffixRule.new();
	}
	TypeRule createInvocationRule(Rule value){
		var caller=NodeRule.new();
		var children=OptionalNodeListRule.new();
		var suffixRule=StripRule.new();
		return TypeRule.new();
	}
	Rule createReturnRule(Rule value){
		return TypeRule.new();
	}
	Rule createValueRule(Rule statement, Rule function){
		var value=LazyRule.new();
		value.set(OrRule.new());
		return value;
	}
	TypeRule createLambdaRule(Rule statement, LazyRule value){
		var args=StripRule.new();
		var rightRule=OrRule.new();
		return TypeRule.new();
	}
	TypeRule createStringRule(){
		var value=PrefixRule.new();
		return TypeRule.new();
	}
	TypeRule createTernaryRule(LazyRule value){
		return TypeRule.new();
	}
	TypeRule createCharRule(){
		return TypeRule.new();
	}
	TypeRule createNumberRule(){
		return TypeRule.new();
	}
	TypeRule createOperatorRule(String type, String operator, LazyRule value){
		return TypeRule.new();
	}
	TypeRule createNotRule(LazyRule value){
		return TypeRule.new();
	}
	TypeRule createConstructionRule(LazyRule value){
		var type=StringRule.new();
		var arguments=OptionalNodeListRule.new();
		var childRule=InfixRule.new();
		return TypeRule.new();
	}
	Rule createSymbolRule(){
		return TypeRule.new();
	}
	Rule createAccessRule(String type, String infix, Rule value){
		var rule=InfixRule.new();
		return TypeRule.new();
	}
	Rule createDefinitionRule(){
		var name=FilterRule.new();
		var typeProperty=NodeRule.new();
		var typeAndName=StripRule.new();
		var modifiers=createModifiersRule();
		var typeParams=StringRule.new();
		var maybeTypeParams=OrRule.new();
		var withModifiers=OptionalNodeListRule.new();
		var annotation=TypeRule.new();
		var annotations=DivideRule.new();
		return TypeRule.new();
	}
	DivideRule createModifiersRule(){
		var modifierRule=TypeRule.new();
		return DivideRule.new();
	}
	Rule createTypeRule(){
		var type=LazyRule.new();
		type.set(OrRule.new());
		return type;
	}
	TypeRule createStructRule(){
		return TypeRule.new();
	}
	TypeRule createSliceRule(LazyRule type){
		return TypeRule.new();
	}
	TypeRule createTupleRule(LazyRule type){
		return TypeRule.new();
	}
	TypeRule createFunctionalRule(Rule type){
		var params=OptionalNodeListRule.new();
		var leftRule=PrefixRule.new();
		var rule=InfixRule.new();
		return TypeRule.new();
	}
	TypeRule createArrayRule(LazyRule type){
		return TypeRule.new();
	}
	TypeRule createVarArgsRule(LazyRule type){
		return TypeRule.new();
	}
	TypeRule createGenericRule(LazyRule type){
		var parent=StringRule.new();
		var children=DivideRule.new();
		return TypeRule.new();
	}struct CommonLang new(){struct CommonLang this;return this;}
}