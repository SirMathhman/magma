import magma.api.result.Result;import magma.app.error.CompileError;struct Passer{
	Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit);
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit);
	struct Passer new(){
		struct Passer this;
		return this;
	}
}