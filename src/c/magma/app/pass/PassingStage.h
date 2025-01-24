import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;struct PassingStage{
	struct Table{
		Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit);
	}
	struct Impl{}
}