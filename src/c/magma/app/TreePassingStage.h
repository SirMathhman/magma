import magma.api.result.Result;import magma.api.stream.Streams;import magma.app.error.CompileError;import java.util.ArrayList;import java.util.List;struct TreePassingStage implements PassingStage{
	Passer passer;
	public TreePassingStage(Passer passer){
		this.passer =passer;
	}
	List<Node> add(PassUnit<List<Node>> unit2, Node value){
		var copy=ArrayList<>.new();
		copy.add(value);
		return copy;
	}
	Result<PassUnit<List<Node>>, CompileError> passAndAdd(PassUnit<List<Node>> unit, Node element){
		return pass(unit.withValue(element)).mapValue(()->result.mapValue(()->add(unit, value)));
	}
	Result<PassUnit<Node>, CompileError> passNodeLists(PassUnit<Node> unit){
		return unit.value().streamNodeLists().foldLeftToResult(unit, (current, tuple) -> {
            final var propertyKey = tuple.left();
            final var propertyValues = tuple.right();
            return Streams.from(propertyValues).foldLeftToResult(current.withValue(ArrayList<>.new()), this::passAndAdd).mapValue(unit1 -> unit1.mapValue(node -> current.value().withNodeList(propertyKey, node)));
        });
	}
	Result<PassUnit<Node>, CompileError> passNodes(PassUnit<Node> unit){
		return unit.value().streamNodes().foldLeftToResult(unit, (current, tuple) -> {
            final var pairKey = tuple.left();
            final var pairNode = tuple.right();

            return pass(current.withValue(pairNode)).mapValue(passed -> passed.mapValue(value -> current.value().withNode(pairKey, value)));
        });
	}
	Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit){
		return this.passer.beforePass(unit).flatMapValue(this::passNodes).flatMapValue(this::passNodeLists).flatMapValue(this.passer::afterPass);
	}
	struct TreePassingStage new(){
		struct TreePassingStage this;
		return this;
	}
}