import magma.api.result.Result;import magma.app.error.CompileError;struct PassingStage{
	struct VTable{
		((void*, PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) pass;
	}
	Box<void*> ref;
	struct VTable vtable;
	struct PassingStage new(Box<void*> ref, struct VTable vtable){
		struct PassingStage this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}