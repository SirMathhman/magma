import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
public struct StringRule implements Rule {private final String propertyKey;public StringRule(String propertyKey){this.propertyKey =propertyKey;}public Result<String, CompileError> parse(Node node){return node.findString(this.propertyKey)
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + this.propertyKey + "' not present", new NodeContext(node))));}@Override
    public Result<Node, CompileError> parse(String input){return new Ok<>(new MapNode().withString(this.propertyKey, input));}@Override
    public Result<String, CompileError> generate(Node node){return node.findString(this.propertyKey)
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + this.propertyKey + "' not present", new NodeContext(node))));}}