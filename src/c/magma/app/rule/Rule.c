import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;struct Rule{
	struct VTable{
		((void*, String) => Result<Node, CompileError>) parse;
		((void*, Node) => Result<String, CompileError>) generate;
	}
	Box<void*> ref;
	struct VTable vtable;
	struct Rule new(Box<void*> ref, struct VTable vtable){
		struct Rule this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}