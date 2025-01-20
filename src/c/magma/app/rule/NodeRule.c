import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
public struct NodeRule(String propertyKey, Rule childRule) implements Rule {@Override
    public Result<Node, CompileError> parse(String input){return this.childRule.parse(input).mapValue(node -> new MapNode().withNode(this.propertyKey, node));}@Override
    public Result<String, CompileError> generate(Node node){return node.findNode(this.propertyKey)
                .map(this.childRule::generate)
                .orElseGet(() -> new Err<>(new CompileError("Node '" + this.propertyKey + "' was not present", new NodeContext(node))));}}