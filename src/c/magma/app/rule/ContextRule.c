#include "./ContextRule.h"
struct ContextRule(String message, Rule childRule) implements Rule{
	Result<Node, CompileError> parse(String input){
		return this.childRule.parse(input).mapErr(()->new CompileError(this.message, new StringContext(input), List.of(err)));
	}
	Result<String, CompileError> generate(Node node){
		return this.childRule.generate(node).mapErr(()->new CompileError(this.message, new NodeContext(node), List.of(err)));
	}
}
