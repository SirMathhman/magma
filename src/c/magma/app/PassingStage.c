import magma.api.result.Result;import magma.app.error.CompileError;struct PassingStage{
	Result<PassUnit<Node>, CompileError> pass(any* _ref_, PassUnit<Node> unit);
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}