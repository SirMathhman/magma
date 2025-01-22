import magma.app.locate.BackwardsLocator;import magma.app.locate.InvocationLocator;import magma.app.rule.ContextRule;import magma.app.rule.ExactRule;import magma.app.rule.FilterRule;import magma.app.rule.InfixRule;import magma.app.rule.LazyRule;import magma.app.rule.NodeRule;import magma.app.rule.OptionalNodeListRule;import magma.app.rule.OptionalNodeRule;import magma.app.rule.OrRule;import magma.app.rule.PrefixRule;import magma.app.rule.Rule;import magma.app.rule.StringRule;import magma.app.rule.StripRule;import magma.app.rule.SuffixRule;import magma.app.rule.TypeRule;import magma.app.rule.divide.DivideRule;import magma.app.rule.divide.SimpleDivider;import magma.app.rule.filter.NumberFilter;import magma.app.rule.filter.SymbolFilter;import magma.app.rule.locate.FirstLocator;import magma.app.rule.locate.LastLocator;import magma.app.rule.locate.ParenthesesMatcher;import java.util.List;import static magma.app.rule.divide.StatementDivider.STATEMENT_DIVIDER;import static magma.app.rule.divide.ValueDivider.VALUE_DIVIDER;struct CommonLang{
	String ROOT_TYPE="root";
	String RECORD_TYPE="record";
	String CLASS_TYPE="class";
	String INTERFACE_TYPE="interface";
	String BEFORE_STRUCT_SEGMENT="before-struct-segment";
	String STRUCT_TYPE="struct";
	String WHITESPACE_TYPE="whitespace";
	String CONTENT_BEFORE_CHILD="content-before-child";
	String PARENT="caller";
	String GENERIC_CHILDREN="generic-children";
	String FUNCTIONAL_TYPE="functional";
	String METHOD_CHILD="method-child";
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
	Rule createNamespacedRule(String type, String prefix);
	Rule createCompoundRule(String type, String infix, Rule segmentRule);
	StripRule createStructSegmentRule(LazyRule function, Rule statement);
	SuffixRule createDefinitionStatementRule();
	Rule createInitializationRule(Rule value);
	Rule createMethodRule(Rule statement);
	Rule wrapUsingBlock(Rule beforeBlock, Rule statement);
	Rule createContentRule(Rule rule);
	Rule createStatementRule(Rule function);
	TypeRule createKeywordRule(String keyword);
	TypeRule createElseRule(LazyRule statement);
	TypeRule createConditionalRule(LazyRule statement, String type, Rule value);
	TypeRule createWhitespaceRule();
	Rule createPostfixRule(String type, String operator, Rule value);
	Rule createAssignmentRule(Rule value);
	Rule createInvocationStatementRule(Rule value);
	TypeRule createInvocationRule(Rule value);
	Rule createReturnRule(Rule value);
	Rule createValueRule(Rule statement, Rule function);
	TypeRule createLambdaRule(Rule statement, LazyRule value);
	TypeRule createStringRule();
	TypeRule createTernaryRule(LazyRule value);
	TypeRule createCharRule();
	TypeRule createNumberRule();
	TypeRule createOperatorRule(String type, String operator, LazyRule value);
	TypeRule createNotRule(LazyRule value);
	TypeRule createConstructionRule(LazyRule value);
	Rule createSymbolRule();
	Rule createAccessRule(String type, String infix, Rule value);
	Rule createDefinitionRule();
	DivideRule createModifiersRule();
	Rule createTypeRule();
	TypeRule createSliceRule(LazyRule type);
	TypeRule createTupleRule(LazyRule type);
	TypeRule createFunctionalRule(Rule type);
	TypeRule createArrayRule(LazyRule type);
	TypeRule createVarArgsRule(LazyRule type);
	TypeRule createGenericRule(LazyRule type);}