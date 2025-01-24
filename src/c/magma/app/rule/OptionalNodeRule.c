#include "magma/api/result/Result.h"
#include "magma/app/Node.h"
#include "magma/app/error/CompileError.h"
#include "java/util/List.h"
struct OptionalNodeRule implements Rule{
	String propertyKey;
	Rule ifPresent;
	Rule ifEmpty;
	OrRule rule;
	public OptionalNodeRule(String propertyKey, Rule ifPresent, Rule ifEmpty){
		this.propertyKey =propertyKey;
		this.ifPresent =ifPresent;
		this.ifEmpty =ifEmpty;
		this.rule = new OrRule(List.of(ifPresent, ifEmpty));
	}
	public OptionalNodeRule(String modifiers, Rule ifPresent){
		this(modifiers, ifPresent, new ExactRule(""));
	}
	Result<Node, CompileError> parse(String input){
		return this.rule.parse(input);
	}
	Result<String, CompileError> generate(Node node){
		if(node.hasNode(this.propertyKey)){
			return this.ifPresent.generate(node);
		}
		else{
			return this.ifEmpty.generate(node);
		}
	}
}
