#include "./StringRule.h"
struct StringRule implements Rule{
	String propertyKey;
	public StringRule(String propertyKey){
		this.propertyKey = propertyKey;
	}
	Result<String, CompileError> parse(Node node){
		return node.findString(this.propertyKey).<Result<String, CompileError>>map(Ok::new).orElseGet(()->new Err<>(new CompileError("String '"+this.propertyKey+"' not present", new NodeContext(node))));
	}
	Result<Node, CompileError> parse(String input){
		return new Ok<>(new MapNode().withString(this.propertyKey, input));
	}
	Result<String, CompileError> generate(Node node){
		return node.findString(this.propertyKey).<Result<String, CompileError>>map(Ok::new).orElseGet(()->new Err<>(new CompileError("String '"+this.propertyKey+"' not present", new NodeContext(node))));
	}
}
