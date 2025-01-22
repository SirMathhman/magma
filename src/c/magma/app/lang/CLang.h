import magma.app.rule.LazyRule;
import magma.app.rule.OrRule;
import magma.app.rule.Rule;
import magma.app.rule.TypeRule;
import java.util.List;

static Rule createCRootRule(){
	return new TypeRule(CommonLang.ROOT_TYPE, CommonLang.createContentRule(createCRootSegmentRule()));
}

static OrRule createCRootSegmentRule(){
	const auto function=new LazyRule();
	return new OrRule(List.of(CommonLang.createNamespacedRule("import", "import "), JavaLang.createJavaCompoundRule(CommonLang.STRUCT_TYPE, "struct ", function), function, CommonLang.createWhitespaceRule()));
}
struct CLang {
}

