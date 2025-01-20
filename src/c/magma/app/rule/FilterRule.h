package magma.app.rule;package magma.api.result.Err;package magma.api.result.Result;package magma.app.Node;package magma.app.error.CompileError;package magma.app.error.context.StringContext;package java.util.function.Predicate;public record FilterRule(Predicate<String> filter,
                         Rule childRule) implements Rule {@Override
    public Result<Node, CompileError> parse(String input){if(this.filter.test(input)){return this.childRule.parse(input);}else {return new Err<>(new CompileError("Filter did not apply", new StringContext(input)));}}@Override
    public Result<String, CompileError> generate(Node node){return this.childRule.generate(node);}}