#include "../../../magma/api/result/Result.h"
#include "../../../magma/app/Node.h"
#include "../../../magma/app/error/CompileError.h"
struct StripRule(Rule childRule, String before, String after) implements Rule{
	public StripRule(Rule childRule){
		this(childRule, "", "");
	}
	Result<Node, CompileError> parse(String input){
		return this.childRule.parse(input.strip());
	}
	Result<String, CompileError> generate(Node node){
		var before=node.findString(this.before).orElse("");
		var after=node.findString(this.after).orElse("");
		return this.childRule.generate(node).mapValue(()->before+content+after);
	}
}
