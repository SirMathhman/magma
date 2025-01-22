import magma.api.result.Err;import magma.api.result.Result;import magma.api.stream.Streams;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;import magma.app.rule.Rule;import java.util.ArrayList;import java.util.List;import java.util.Optional;struct DivideRule implements Rule{
String propertyKey;
Divider divider;
Rule childRule;
public DivideRule(String propertyKey, Divider divider, Rule childRule);
<T, R>Result<List<R>, CompileError> compileAll(List<T> segments, Function<T, Result<R, CompileError>> mapper);
Result<Node, CompileError> parse(String input);
Result<String, CompileError> generate(Node node);
String merge(List<String> elements);
}