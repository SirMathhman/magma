#include "magma/api/result/Err.h"
#include "magma/api/result/Result.h"
#include "magma/app/MapNode.h"
#include "magma/app/Node.h"
#include "magma/app/error/CompileError.h"
#include "magma/app/error/context/NodeContext.h"
#include "magma/app/error/context/StringContext.h"
#include "java/util/List.h"
struct TypeRule(String type, Rule rule) implements Rule{
	Result<Node, CompileError> parse(String input){
		return this.rule.parse(input).mapValue(()->node.retype(this.type)).mapValue(()->{
			if(type.equals("method")){
				System.out.println("\t"+node.findNode("definition").orElse(new MapNode()).findString("name").orElse(""));
			}
			return node;
		}).mapErr(()->new CompileError("Failed to parse type '"+this.type + "'", new StringContext(input), List.of(err)));
	}
	Result<String, CompileError> generate(Node node){
		if(node.is(this.type)){
			return this.rule.generate(node).mapErr(()->new CompileError("Failed to generate type '"+this.type + "'", new NodeContext(node), List.of(err)));
		}
		else{
			return new Err<>(new CompileError("Node was not of type '"+this.type + "'", new NodeContext(node)));
		}
	}
}
