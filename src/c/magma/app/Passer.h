import magma.api.result.Result;import magma.app.error.CompileError;struct Passer{
	((PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) afterPass;
	((PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) beforePass;
	struct Passer new(){
		struct Passer this;
		return this;
	}
}