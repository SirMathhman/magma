struct CLang{
	Rule createCRootRule(){
		return new TypeRule(CommonLang.ROOT_TYPE, CommonLang.createContentRule(createCRootSegmentRule()));
	}
	OrRule createCRootSegmentRule(){
		var function=new LazyRule();
		var struct=new LazyRule();
		struct.set(JavaLang.createJavaCompoundRule(CommonLang.STRUCT_TYPE, "struct ", function, struct));
		return new OrRule(List.of(CommonLang.createNamespacedRule("include", "#include \"", "/", ".h\""), struct, function, CommonLang.createWhitespaceRule()));
	}
}
