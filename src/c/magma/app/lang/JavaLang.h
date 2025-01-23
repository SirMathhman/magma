import magma.app.rule.LazyRule;import magma.app.rule.OrRule;import magma.app.rule.Rule;import magma.app.rule.TypeRule;import java.util.List;struct JavaLang{
	Rule createJavaRootRule(){
		return TypeRule.new();
	}
	OrRule createJavaRootSegmentRule(){
		var function=LazyRule.new();
		var struct=LazyRule.new();
		struct.set(OrRule.new());
		return OrRule.new();
	}
	Rule createJavaCompoundRule(String type, String infix, LazyRule function, LazyRule struct){
		var statement=CommonLang.createStatementRule(function, struct);
		function.set(CommonLang.createMethodRule(statement));
		return CommonLang.createCompoundRule(type, infix, CommonLang.createStructSegmentRule(function, statement, struct));
	}
	struct JavaLang new(){
		struct JavaLang this;
		return this;
	}
}