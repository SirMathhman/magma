import magma.api.result.Err;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.Context;
import magma.app.error.context.NodeContext;
import magma.app.error.context.StringContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
struct OrRule(List<Rule> rules) implements Rule {
	@Override
Result<Node, CompileError> parse(String value){
		return process(new StringContext(value), auto temp(){
			return rule;
		}.parse(value));
	}
	<R>Result<R, CompileError> process(Context context, Function<Rule, Result<R, CompileError>> mapper){
		return Streams.from(this.rules).map(auto temp(){
			return mapper;
		}.apply(rule).mapErr(Collections.singletonList)).foldLeft(auto temp(){
			return first;
		}.or(auto temp(){
			return second;
		}).mapErr(auto temp(){
			 auto left=new ArrayList<>(tuple.left());
			left.addAll(tuple.right());
			return left;
		})).orElseGet(auto temp(){
			return new Err<>(Collections.singletonList(new CompileError("No rules set", context)));
		}).mapErr(errors -> new CompileError("No valid rule", context, errors));
	}
	@Override
Result<String, CompileError> generate(Node node){
		return process(new NodeContext(node), auto temp(){
			return rule;
		}.generate(node));
	}
}

