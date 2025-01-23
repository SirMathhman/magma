import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;struct Rule{
	struct VTable{
		Result<Node, CompileError> (*)(void*, String) parse;
		Result<String, CompileError> (*)(void*, Node) generate;
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