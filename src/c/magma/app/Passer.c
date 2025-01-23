import magma.api.result.Result;import magma.app.error.CompileError;struct Passer{
	struct VTable{
		((Any, PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) afterPass;
		((Any, PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) beforePass;
	}
	Box<Any> ref;
	struct VTable vtable;
	struct Passer new(Box<Any> ref, struct VTable vtable){
		struct Passer this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}