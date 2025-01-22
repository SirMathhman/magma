import magma.api.result.Err;import magma.api.result.Result;import magma.api.stream.Streams;import magma.app.MapNode;import magma.app.Node;import magma.app.error.CompileError;import magma.app.error.context.NodeContext;import magma.app.rule.Rule;import java.util.ArrayList;import java.util.List;import java.util.Optional;struct DivideRule implements Rule {String propertyKey;Divider divider;Rule childRule;public DivideRule(String propertyKey, Divider divider, Rule childRule){
	this.divider =divider;
	this.childRule =childRule;
	this.propertyKey =propertyKey;}<T, R>Result<List<R>, CompileError> compileAll(List<T> segments, Function<T, Result<R, CompileError>> mapper){
	return Streams.from(segments).foldLeftToResult(new ArrayList<>(), (rs, t) -> mapper.apply(t).mapValue(inner -> {
            rs.add(inner);
            return rs;
        }));}Result<Node, CompileError> parse(String input){
	return this.divider.divide(input).flatMapValue(()->compileAll(segments, this.childRule::parse)).mapValue(()->{
	var node=new MapNode();
	return segments.isEmpty() ? node : node.withNodeList(this.propertyKey, segments);});}Result<String, CompileError> generate(Node node){
	return node.findNodeList(this.propertyKey).flatMap(()->list.isEmpty() ? Optional.empty() : Optional.of(list)).map(()->compileAll(list, this.childRule::generate)).map(()->result.mapValue(this::merge)).orElseGet(()->new Err<>(new CompileError("Node list '"+this.propertyKey + "' not present", new NodeContext(node))));}String merge(List<String> elements){
	return Streams.from(elements).foldLeft(this.divider::merge).orElse("");}}