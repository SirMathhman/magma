import magma.app.locate.BackwardsLocator;import magma.app.locate.InvocationLocator;import magma.app.rule.ContextRule;import magma.app.rule.ExactRule;import magma.app.rule.FilterRule;import magma.app.rule.InfixRule;import magma.app.rule.LazyRule;import magma.app.rule.NodeRule;import magma.app.rule.OptionalNodeListRule;import magma.app.rule.OptionalNodeRule;import magma.app.rule.OrRule;import magma.app.rule.PrefixRule;import magma.app.rule.Rule;import magma.app.rule.StringRule;import magma.app.rule.StripRule;import magma.app.rule.SuffixRule;import magma.app.rule.TypeRule;import magma.app.rule.divide.DivideRule;import magma.app.rule.divide.SimpleDivider;import magma.app.rule.filter.NumberFilter;import magma.app.rule.filter.SymbolFilter;import magma.app.rule.locate.FirstLocator;import magma.app.rule.locate.LastLocator;import magma.app.rule.locate.ParenthesesMatcher;import java.util.List;import static magma.app.rule.divide.StatementDivider.STATEMENT_DIVIDER;import static magma.app.rule.divide.ValueDivider.VALUE_DIVIDER;struct CommonLang {
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
static const String METHOD_TYPE="method";
static const String INITIALIZATION_TYPE="initialization";
static const String METHOD_DEFINITION="definition";
static const String INITIALIZATION_VALUE="value";
static const String INITIALIZATION_DEFINITION="definition";
static const String DEFINITION_TYPE="definition";
static const String TUPLE_TYPE="tuple";
static const String TUPLE_CHILDREN="children";
static const String CONTENT_AFTER_CHILD="after-child";
}