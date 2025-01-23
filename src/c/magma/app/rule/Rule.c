import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;struct Rule{
	struct VTable{
		((String) => Result<Node, CompileError>) parse;
		((Node) => Result<String, CompileError>) generate;
	}
	struct VTable vtable;
	struct Rule new(struct VTable table){
		struct Rule this;
		this.table=table;
		return this;
	}
}