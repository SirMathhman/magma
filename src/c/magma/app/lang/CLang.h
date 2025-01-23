import magma.app.rule.LazyRule;import magma.app.rule.OrRule;import magma.app.rule.Rule;import magma.app.rule.TypeRule;import java.util.List;struct CLang{
	Rule createCRootRule(){
		return new TypeRule(CommonLang.ROOT_TYPE, CommonLang.createContentRule(createCRootSegmentRule()));
	}
	OrRule createCRootSegmentRule(){
		var function=new LazyRule();
		var struct=new LazyRule();
		struct.set(JavaLang.createJavaCompoundRule(CommonLang.STRUCT_TYPE, "struct ", function, struct));
		return new OrRule(List.of(CommonLang.createNamespacedRule("import", "import "), struct, function, CommonLang.createWhitespaceRule()));
	}
}