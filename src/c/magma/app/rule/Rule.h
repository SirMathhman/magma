import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;struct Rule{
	struct VTable{
		((Any, String) => Result<Node, CompileError>) parse;
		((Any, Node) => Result<String, CompileError>) generate;
	}
	Box<Any> ref;
	struct VTable vtable;
	struct Rule new(Box<Any> ref, struct VTable vtable){
		struct Rule this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}