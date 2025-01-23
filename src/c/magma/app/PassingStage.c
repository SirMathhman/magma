import magma.api.result.Result;import magma.app.error.CompileError;struct PassingStage{
	struct VTable{
		((PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) pass;
	}
	struct PassingStage new(){
		struct PassingStage this;
		return this;
	}
}