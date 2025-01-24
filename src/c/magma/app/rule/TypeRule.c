#include "./TypeRule.h"
struct TypeRule(String type, Rule rule) implements Rule{
	Result<Node, CompileError> parse(String input){
		return this.rule.parse(input).mapValue(node->node.retype(this.type)).mapValue(this::postProcess).mapErr(err->new CompileError("Failed to parse type '"+this.type+"'", new StringContext(input), List.of(err)));
	}
	Node postProcess(Node node){
		if(this.type.equals("method")){
			out.println("\t"+node.findNode("definition").orElse(new MapNode()).findString("name").orElse(""));
		}
		return node;
	}
	Result<String, CompileError> generate(Node node){
		if(node.is(this.type)){
			return this.rule.generate(node).mapErr(err->new CompileError("Failed to generate type '"+this.type+"'", new NodeContext(node), List.of(err)));
		}
		else{
			return new Err<>(new CompileError("Node was not of type '"+this.type+"'", new NodeContext(node)));
		}
	}
}
