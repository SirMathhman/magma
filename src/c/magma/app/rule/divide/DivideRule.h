import magma.api.result.Err;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
import magma.app.rule.Rule;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
public struct DivideRule implements Rule {
	private final String propertyKey;
	private final Divider divider;
	private final Rule childRule;
	((String, Divider, Rule) => public) DivideRule=public DivideRule(String propertyKey, Divider divider, Rule childRule){
		this.divider =divider;
		this.childRule =childRule;
		this.propertyKey =propertyKey;
	};
	<T, R>((List<T>, ((T) => Result<R, CompileError>)) => Result<List<R>, CompileError>) compileAll=<T, R>Result<List<R>, CompileError> compileAll(List<T> segments, ((T) => Result<R, CompileError>) mapper){
		return Streams.from(segments).foldLeftToResult(new ArrayList<>(), (rs, t) ->mapper.apply(t).mapValue(inner ->{
		rs.add(inner);
		return rs;
	}));
	};
	((String) => Result<Node, CompileError>) parse=Result<Node, CompileError> parse(String input){
		return this.divider.divide(input).flatMapValue(segments -> compileAll(segments, this.childRule::parse))
                .mapValue(segments -> new MapNode().withNodeList(this.propertyKey, segments));
	};
	((Node) => Result<String, CompileError>) generate=Result<String, CompileError> generate(Node node){
		return node.findNodeList(this.propertyKey)
                .flatMap(list ->list.isEmpty() ? Optional.empty() : Optional.of(list))
                .map(list -> compileAll(list, this.childRule::generate))
                .map(result ->result.mapValue(this::merge)).orElseGet(() ->new Err<>(new CompileError("Node list '"+this.propertyKey + "' not present", new NodeContext(node))));
	};
	((List<String>) => String) merge=String merge(List<String> elements){
		return Streams.from(elements).foldLeft(this.divider::merge).orElse("");
	};
}