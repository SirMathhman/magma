import magma.api.result.Result;import magma.app.error.CompileError;struct PassingStage{
	Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit);
	struct PassingStage new(){
		struct PassingStage this;
		return this;
	}
}