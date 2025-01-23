import magma.api.result.Result;import magma.app.error.CompileError;struct Passer{
	struct VTable{
		Result<PassUnit<Node>, CompileError> (*)(void*, PassUnit<Node>) afterPass;
		Result<PassUnit<Node>, CompileError> (*)(void*, PassUnit<Node>) beforePass;
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