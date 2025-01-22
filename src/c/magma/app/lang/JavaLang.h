import magma.app.rule.LazyRule;import magma.app.rule.OrRule;import magma.app.rule.Rule;import magma.app.rule.TypeRule;import java.util.List;struct JavaLang{
	Rule createJavaRootRule();
	OrRule createJavaRootSegmentRule();
	Rule createJavaCompoundRule(String type, String infix, LazyRule function);}