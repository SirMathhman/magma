#include "magma/api/result/Err.h"
#include "magma/api/result/Result.h"
#include "magma/app/MapNode.h"
#include "magma/app/Node.h"
#include "magma/app/error/CompileError.h"
#include "magma/app/error/context/NodeContext.h"
struct NodeRule(String propertyKey, Rule childRule) implements Rule{
	Result<Node, CompileError> parse(String input){
		return this.childRule.parse(input).mapValue(()->new MapNode().withNode(this.propertyKey, node));
	}
	Result<String, CompileError> generate(Node node){
		return node.findNode(this.propertyKey).map(this.childRule::generate).orElseGet(()->new Err<>(new CompileError("Node '"+this.propertyKey + "' was not present", new NodeContext(node))));
	}
}
