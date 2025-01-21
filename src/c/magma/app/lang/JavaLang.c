import magma.app.rule.OrRule;
import magma.app.rule.Rule;
import magma.app.rule.TypeRule;
import java.util.List;
public struct JavaLang {public static Rule createJavaRootRule(){
	return new TypeRule(CommonLang.ROOT_TYPE, CommonLang.createContentRule(createJavaRootSegmentRule()));
}private static OrRule createJavaRootSegmentRule(){
	return new OrRule(List.of(CommonLang.createNamespacedRule("package", "package "), CommonLang.createNamespacedRule("import", "import "), createJavaCompoundRule(CommonLang.CLASS_TYPE, "class "), createJavaCompoundRule(CommonLang.RECORD_TYPE, "record "), createJavaCompoundRule(CommonLang.INTERFACE_TYPE, "interface "), CommonLang.createWhitespaceRule()));
}public static Rule createJavaCompoundRule(String type, String infix){
	return CommonLang.createCompoundRule(type, infix, CommonLang.createStructSegmentRule());
}}

