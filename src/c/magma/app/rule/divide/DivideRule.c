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
struct DivideRule implements Rule {
	 String propertyKey;
	 Divider divider;
	 Rule childRule;
	public DivideRule(String propertyKey, Divider divider, Rule childRule){
		this.divider =divider;
		this.childRule =childRule;
		this.propertyKey =propertyKey;
	}
	 <T, R>Result<List<R>, CompileError> compileAll(List<T> segments, Function<T, Result<R, CompileError>> mapper){
		return Streams.from(segments).foldLeftToResult(new ArrayList<>(), (rs, t) -> mapper.apply(t).mapValue(inner -> {
            rs.add(inner);
            return rs;
        }));
	}
	@Override
Result<Node, CompileError> parse(String input){
		return this.divider.divide(input).flatMapValue(segments -> compileAll(segments, this.childRule::parse))
                .mapValue(segments -> {
                    final var node = new MapNode();
                    return segments.isEmpty() ? node : node.withNodeList(this.propertyKey, segments);
                });
	}
	@Override
Result<String, CompileError> generate(Node node){
		return node.findNodeList(this.propertyKey).flatMap(auto _lambda28_(auto list){
			return list.isEmpty() ? Optional.empty() : Optional.of(list);
		}).map(list -> compileAll(list, this.childRule::generate))
                .map(auto _lambda29_(auto result){
			return result.mapValue(this.merge);
		}).orElseGet(auto _lambda30_(){
			return new Err<>(new CompileError("Node list '"+this.propertyKey + "' not present", new NodeContext(node)));
		});
	}
	String merge(List<String> elements){
		return Streams.from(elements).foldLeft(this.divider::merge).orElse("");
	}
}

