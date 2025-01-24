#include "./CLang.h"
struct CLang{
	Rule createCRootRule(){
		return new TypeRule(CommonLang.ROOT_TYPE, CommonLang.createContentRule(createCRootSegmentRule()));
	}
	OrRule createCRootSegmentRule(){
		var function=new LazyRule();
		var struct=new LazyRule();
		struct.set(JavaLang.createJavaCompoundRule(CommonLang.STRUCT_TYPE, "struct ", function, struct));
		return new OrRule(List.of(CommonLang.createNamespacedRule("include", "#include \"", "/", ".h\""), new TypeRule("if-not-defined", new PrefixRule("#ifndef ", new StringRule("value"))), new TypeRule("define", new PrefixRule("#define ", new StringRule("value"))), new TypeRule("endif", new ExactRule("#endif")), struct, function, CommonLang.createWhitespaceRule()));
	}
}
