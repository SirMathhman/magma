import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
import magma.app.rule.locate.Locator;
import java.util.List;
public final struct InfixRule implements Rule {
	private final Rule leftRule;
	private final Locator locator;
	private final Rule rightRule;
	public InfixRule(Rule leftRule, Locator locator, Rule rightRule){
		this.leftRule =leftRule;
		this.locator =locator;
		this.rightRule =rightRule;
	}
	public static Result<Tuple<String, String>, CompileError> split(Locator locator, String input){
		return locator.locate(input).<Result<Tuple<String, String>, CompileError>>map(index -> {
            final var left=input.substring(0, index);
            final var right = input.substring(index + locator.length());
            return new Ok<>(new Tuple<String, String>(left, right));
        }).orElseGet(() ->new Err<>(new CompileError("Infix '"+locator.unwrap() + "' not present", new StringContext(input))));
	}
	private static CompileError invalidate(CompileError err, String left, String type){
		return new CompileError("Failed to process "+type, new StringContext(left), List.of(err));
	}
	@Override
    public Result<String, CompileError> generate(Node node){
		return this.leftRule.generate(node).and(
                () ->this.rightRule.generate(node)).mapValue(Tuple.merge(
                (left, right) ->left+this.locator.unwrap() + right));
	}
	@Override
    public Result<Node, CompileError> parse(String input){
		return split(this.locator, input).flatMapValue(tuple -> {
            final var left=tuple.left();
            final var right = tuple.right();
            return this.leftRule.parse(left)
                    .mapErr(err -> invalidate(err, left, "left"))
                    .and(() -> this.rightRule.parse(right).mapErr(err -> invalidate(err, right, "right")))
                    .mapValue(Tuple.merge(Node::merge));
        });
	}
}