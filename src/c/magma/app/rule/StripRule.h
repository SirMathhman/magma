package magma.app.rule;package magma.api.result.Result;package magma.app.Node;package magma.app.error.CompileError;public record StripRule(
        Rule childRule) implements Rule {@Override
    public Result<Node, CompileError> parse(String input){return this.childRule.parse(input.strip());}@Override
    public Result<String, CompileError> generate(Node node){return this.childRule.generate(node);}}