import magma.api.result.Err;import magma.api.result.Ok;import magma.api.result.Result;import magma.api.stream.Streams;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;import magma.app.error.context.StringContext;import magma.app.rule.Rule;import java.util.ArrayList;import java.util.List;import java.util.Optional;struct DivideRule{
	String propertyKey;
	Divider divider;
	Rule childRule;
	public DivideRule(any* _ref_, String propertyKey, Divider divider, Rule childRule){
		this.divider =divider;
		this.childRule =childRule;
		this.propertyKey =propertyKey;
	}
	<T, R>Result<List<R>, CompileError> compileAll(any* _ref_, List<T> segments, Tuple<any*, Result<R, CompileError> (*)(any*, T)> mapper, Tuple<any*, Result<R, CompileError> (*)(any*, T, R)> validator){
		return Streams.from(segments).foldLeftToResult(new ArrayList<>(), (rs, t) -> {
            return mapper.apply(t).flatMapValue(()->validator.apply(t, inner)).mapValue(inner -> {
                        rs.add(inner);
                        return rs;
                    });
        });
	}
	Result<Node, CompileError> validateNode(any* _ref_, String text, Node result){
		if(result.hasType()){
			return new Ok<>(result);
		}
		else{
			return new Err<>(new CompileError("Node has no type assigned", new NodeContext(result)));
		}
	}
	Result<Node, CompileError> parse(any* _ref_, String input){
		return this.divider.divide(input).flatMapValue(()->compileAll(segments, this.childRule::parse, DivideRule::validateNode)).mapValue(()->{
			var node=new MapNode();
			return segments.isEmpty() ? node : node.withNodeList(this.propertyKey, segments);
		});
	}
	Result<String, CompileError> generate(any* _ref_, Node node){
		return node.findNodeList(this.propertyKey).flatMap(()->list.isEmpty() ? Optional.empty() : Optional.of(list)).map(list -> compileAll(list, this.childRule::generate, (_, result) -> new Ok<>(result)))
                .map(()->result.mapValue(this::merge)).orElseGet(()->new Err<>(new CompileError("Node list '"+this.propertyKey + "' not present", new NodeContext(node))));
	}
	String merge(any* _ref_, List<String> elements){
		return Streams.from(elements).foldLeft(this.divider::merge).orElse("");
	}
	Rule N/A(){
		return N/A.new();
	}
}