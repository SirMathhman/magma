struct PassingStage{
	Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit);
}
