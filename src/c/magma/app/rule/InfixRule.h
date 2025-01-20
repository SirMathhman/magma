import magma.api.Tuple;import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.StringContext;import magma.app.locate.Locator;public final class InfixRule implements Rule {private final Rule leftRule;private final Locator locator;private final Rule rightRule;public InfixRule(Rule leftRuleRule leftRule Locator locatorRule leftRule Locator locator Rule rightRule){this.leftRule = leftRule;this.locator = locator;this.rightRule = rightRule;}public static Result<Tuple<String, String>, CompileError> split(Locator locatorLocator locator String input){return locator.locate(input).<Result<Tuple<String, String>, CompileError>>map(index -> {
            final var left = input.substring(0, index);
            final var right = input.substring(index + locator.length());
            return new Ok<>(new Tuple<String, String>(left, right));
        }).orElseGet(() -> new Err<>(new CompileError("Infix '" + locator.unwrap() + "' not present", new StringContext(input))));}@Override
    public Result<String, CompileError> generate(Node node){return this.leftRule.generate(node).and(
                () -> this.rightRule.generate(node)).mapValue(Tuple.merge(
                (left, right) -> left + this.locator.unwrap() + right));}@Override
    public Result<Node, CompileError> parse(String input){return split(this.locator, input).flatMapValue(
                tuple -> this.leftRule.parse(tuple.left()).and(
                        () -> this.rightRule.parse(tuple.right())).mapValue(Tuple.merge(Node::merge)));}}