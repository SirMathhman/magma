#include "magma/app/rule/LazyRule.h"
#include "magma/app/rule/OrRule.h"
#include "magma/app/rule/Rule.h"
#include "magma/app/rule/TypeRule.h"
#include "java/util/List.h"
struct JavaLang{
	Rule createJavaRootRule(){
		return new TypeRule(CommonLang.ROOT_TYPE, CommonLang.createContentRule(createJavaRootSegmentRule()));
	}
	OrRule createJavaRootSegmentRule(){
		var function=new LazyRule();
		var struct=new LazyRule();
		struct.set(new OrRule(List.of(createJavaCompoundRule(CommonLang.CLASS_TYPE, "class ", function, struct), createJavaCompoundRule(CommonLang.RECORD_TYPE, "record ", function, struct), createJavaCompoundRule(CommonLang.INTERFACE_TYPE, "interface ", function, struct))));
		return new OrRule(List.of(CommonLang.createNamespacedRule("package", "package ", ".", ";"), CommonLang.createNamespacedRule("import", "import ", ".", ";"), CommonLang.createWhitespaceRule(), struct));
	}
	Rule createJavaCompoundRule(String type, String infix, LazyRule function, LazyRule struct){
		var statement=CommonLang.createStatementRule(function, struct);
		function.set(CommonLang.createMethodRule(statement));
		return CommonLang.createCompoundRule(type, infix, CommonLang.createStructSegmentRule(function, statement, struct));
	}
}
