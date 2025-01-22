import magma.api.result.Err;import magma.api.result.Result;import magma.api.stream.Streams;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.Context;import magma.app.error.context.NodeContext;import magma.app.error.context.StringContext;import java.util.ArrayList;import java.util.Collections;import java.util.List;struct OrRule(List<Rule> rules) implements Rule{
	Result<Node, CompileError> parse(String value);
	<R>Result<R, CompileError> process(Context context, ((Rule) => Result<R, CompileError>) mapper);
	Result<String, CompileError> generate(Node node);
}