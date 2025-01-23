import magma.api.result.Result;import magma.app.error.CompileError;struct Passer{
	struct VTable{
		((void*, PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) afterPass;
		((void*, PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) beforePass;
	}
	Box<void*> ref;
	struct VTable vtable;
	struct Passer new(Box<void*> ref, struct VTable vtable){
		struct Passer this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}