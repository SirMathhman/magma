import magma.api.result.Err;import magma.api.result.Result;import magma.api.stream.Streams;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.Context;import magma.app.error.context.NodeContext;import magma.app.error.context.StringContext;import java.util.ArrayList;import java.util.Collections;import java.util.List;struct OrRule(List<Rule> rules) implements Rule{
	Result<Node, CompileError> parse(String value){
		return process(StringContext.new(), ()->rule.parse(value));
	}
	<R>Result<R, CompileError> process(Context context, [void*, Result<R, CompileError> (*)(void*, Rule)] mapper){
		return Streams.from(this.rules).map(()->mapper.apply(rule).mapErr(Collections::singletonList)).foldLeft((first, second) -> first.or(()->second).mapErr(tuple -> {
                    final var left = new ArrayList<>(tuple.left());
                    left.addAll(tuple.right());
                    return left;
                }))
                .orElseGet(()->Err<>.new()).mapErr(()->CompileError.new());
	}
	Result<String, CompileError> generate(Node node){
		return process(NodeContext.new(), ()->rule.generate(node));
	}
	struct OrRule new(){
		struct OrRule this;
		return this;
	}
}