import magma.api.result.Result;import magma.app.error.CompileError;struct Passer{
	struct VTable{
		((PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) afterPass;
		((PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) beforePass;
	}
	struct Passer new(struct VTable table){
		struct Passer this;
		return this;
	}
}