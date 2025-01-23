import magma.api.result.Result;import magma.app.error.CompileError;struct PassingStage{
	((PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) pass;
	struct PassingStage new(){
		struct PassingStage this;
		return this;
	}
}