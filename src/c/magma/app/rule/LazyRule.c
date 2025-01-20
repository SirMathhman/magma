package magma.app.rule;package magma.api.result.Err;package magma.api.result.Ok;package magma.api.result.Result;package magma.app.Node;package magma.app.error.CompileError;package magma.app.error.context.Context;package magma.app.error.context.NodeContext;package magma.app.error.context.StringContext;package java.util.Optional;public class LazyRule implements Rule {private Optional<Rule> childRule = Optional.empty();@Override
    public Result<Node, CompileError> parse(String input){return findChild(new StringContext(input)).flatMapValue(rule -> rule.parse(input));}private Result<Rule, CompileError> findChild(Context context);@Override
    public Result<String, CompileError> generate(Node node){return findChild(new NodeContext(node)).flatMapValue(rule -> rule.generate(node));}public void set(Rule childRule){this.childRule = Optional.of(childRule);}}