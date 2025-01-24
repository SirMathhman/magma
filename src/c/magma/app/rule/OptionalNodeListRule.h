#include "../../../magma/api/result/Result.h"
#include "../../../magma/app/Node.h"
#include "../../../magma/app/error/CompileError.h"
#include "../../../magma/app/rule/divide/DivideRule.h"
#include "../../../java/util/List.h"
struct OptionalNodeListRule implements Rule{
	String propertyKey;
	Rule ifPresent;
	Rule ifEmpty;
	OrRule rule;
	public OptionalNodeListRule(String propertyKey, Rule ifPresent, Rule ifEmpty){
		this.propertyKey = propertyKey;
		this.ifPresent = ifPresent;
		this.ifEmpty = ifEmpty;
		this.rule = new OrRule(List.of(ifPresent, ifEmpty));
	}
	public OptionalNodeListRule(String propertyKey, DivideRule ifPresent){
		this(propertyKey, ifPresent, new ExactRule(""));
	}
	Result<Node, CompileError> parse(String input){
		return this.rule.parse(input);
	}
	Result<String, CompileError> generate(Node node){
		if(node.hasNodeList(this.propertyKey)){
			return this.ifPresent.generate(node);
		}
		else{
			return this.ifEmpty.generate(node);
		}
	}
}
