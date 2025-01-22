import magma.app.rule.LazyRule;import magma.app.rule.OrRule;import magma.app.rule.Rule;import magma.app.rule.TypeRule;import java.util.List;struct JavaLang{
	Rule createJavaRootRule(){
		return TypeRule.new();
	}
	OrRule createJavaRootSegmentRule(){
		var function=LazyRule.new();
		return OrRule.new();
	}
	Rule createJavaCompoundRule(String type, String infix, LazyRule function){
		var statement=CommonLang.createStatementRule(function);
		function.set(CommonLang.createMethodRule(statement));
		return CommonLang.createCompoundRule(type, infix, CommonLang.createStructSegmentRule(function, statement));
	}
}