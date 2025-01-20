package magma.app.rule;package magma.api.result.Err;package magma.api.result.Ok;package magma.api.result.Result;package magma.app.MapNode;package magma.app.Node;package magma.app.error.CompileError;package magma.app.error.context.NodeContext;public class StringRule implements Rule {private final String propertyKey;public StringRule(String propertyKey){this.propertyKey = propertyKey;}public Result<String, CompileError> parse(Node node){return node.findString(this.propertyKey)
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + this.propertyKey + "' not present", new NodeContext(node))));}@Override
    public Result<Node, CompileError> parse(String input){return new Ok<>(new MapNode().withString(this.propertyKey, input));}@Override
    public Result<String, CompileError> generate(Node node){return node.findString(this.propertyKey)
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + this.propertyKey + "' not present", new NodeContext(node))));}}