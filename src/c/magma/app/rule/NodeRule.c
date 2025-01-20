package magma.app.rule;package magma.api.result.Err;package magma.api.result.Result;package magma.app.MapNode;package magma.app.Node;package magma.app.error.CompileError;package magma.app.error.context.NodeContext;public record NodeRule(String propertyKey, Rule childRule) implements Rule {@Override
    public Result<Node, CompileError> parse(String input){return this.childRule.parse(input).mapValue(node -> new MapNode().withNode(this.propertyKey, node));}@Override
    public Result<String, CompileError> generate(Node node){return node.findNode(this.propertyKey)
                .map(this.childRule::generate)
                .orElseGet(() -> new Err<>(new CompileError("Node '" + this.propertyKey + "' was not present", new NodeContext(node))));}}