import magma.api.result.Ok;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;struct Passer{
	Predicate<Node> by(String type){
		return ()->value.is(type);
	}
	Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit){
		return new Ok<>(unit);
	}
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
		return new Ok<>(unit);
	}
}