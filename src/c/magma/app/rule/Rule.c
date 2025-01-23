import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;struct Rule{
	struct VTable{
		((String) => Result<Node, CompileError>) parse;
		((Node) => Result<String, CompileError>) generate;
	}
	struct Rule new(struct VTable table){
		struct Rule this;
		return this;
	}
}