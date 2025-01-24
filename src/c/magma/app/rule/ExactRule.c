#include "./ExactRule.h"
struct ExactRule(String slice) implements Rule{
	Result<Node, CompileError> parse(String input){
		if(input.equals(this.slice))return new Ok<>(new MapNode());
		var context=new StringContext(input);
		return new Err<>(new CompileError("Exact string '"+this.slice + "' was not present", context));
	}
	Result<String, CompileError> generate(Node node){
		return new Ok<>(this.slice);
	}
}
