package magma.app.rule;package magma.api.result.Err;package magma.api.result.Result;package magma.app.Node;package magma.app.error.CompileError;package magma.app.error.context.NodeContext;package magma.app.error.context.StringContext;package java.util.List;public record TypeRule(String type, Rule rule) implements Rule {@Override
    public Result<Node, CompileError> parse(String input){return this.rule.parse(input)
                .mapValue(node -> node.retype(this.type))
                .mapErr(err -> new CompileError("Failed to parse type '" + this.type + "'", new StringContext(input), List.of(err)));}@Override
    public Result<String, CompileError> generate(Node node){if(node.is(this.type)){return this.rule.generate(node)
                    .mapErr(err -> new CompileError("Failed to generate type '" + this.type + "'", new NodeContext(node), List.of(err)));}else {return new Err<>(new CompileError("Node was not of type '" + this.type + "'", new NodeContext(node)));}}}