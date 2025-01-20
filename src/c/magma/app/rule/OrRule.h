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
public struct OrRule(List<Rule> rules) implements Rule {
	((String) => Result<Node, CompileError>) parse=Result<Node, CompileError> parse(String value){
		return process(new StringContext(value), rule ->rule.parse(value));
	};
	<R>((Context, [Capture, ((Capture, Rule) => Result<R, CompileError>)]) => Result<R, CompileError>) process=<R>Result<R, CompileError> process(Context context, [Capture, ((Capture, Rule) => Result<R, CompileError>)] mapper){
		return Streams.from(this.rules).map(rule ->mapper.apply(rule).mapErr(Collections::singletonList)).foldLeft((first, second) ->first.or(() ->second).mapErr(tuple ->{
		final var left=new ArrayList<>(tuple.left());
		left.addAll(tuple.right());
		return left;
	})).orElseGet(() ->new Err<>(Collections.singletonList(new CompileError("No rules set", context)))).mapErr(errors -> new CompileError("No valid rule", context, errors));
	};
	((Node) => Result<String, CompileError>) generate=Result<String, CompileError> generate(Node node){
		return process(new NodeContext(node), rule ->rule.generate(node));
	};
}