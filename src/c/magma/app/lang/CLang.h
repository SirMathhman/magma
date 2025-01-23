import magma.app.rule.LazyRule;import magma.app.rule.OrRule;import magma.app.rule.Rule;import magma.app.rule.TypeRule;import java.util.List;struct CLang{
	Rule createCRootRule(){
		return TypeRule.new();
	}
	OrRule createCRootSegmentRule(){
		var function=LazyRule.new();
		var struct=LazyRule.new();
		struct.set(JavaLang.createJavaCompoundRule(CommonLang.STRUCT_TYPE, "struct ", function, struct));
		return OrRule.new();
	}
	struct CLang new(){
		struct CLang this;
		return this;
	}
}