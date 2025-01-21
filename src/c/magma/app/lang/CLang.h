import magma.app.rule.OrRule;
import magma.app.rule.Rule;
import magma.app.rule.TypeRule;
import java.util.List;
struct CLang {
	static Rule createCRootRule(){
		return new TypeRule(CommonLang.ROOT_TYPE, CommonLang.createContentRule(createCRootSegmentRule()));
	}
	static OrRule createCRootSegmentRule(){
		return new OrRule(List.of(CommonLang.createNamespacedRule("import", "import "), JavaLang.createJavaCompoundRule(CommonLang.STRUCT_TYPE, "struct "), CommonLang.createWhitespaceRule()));
	}
}

