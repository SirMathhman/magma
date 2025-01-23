import magma.api.result.Result;import magma.app.error.CompileError;struct PassingStage<Capture>{
	struct VTable{
		((PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) pass;
	}
	struct VTable vtable;
	struct PassingStage new(struct VTable table){
		struct PassingStage this;
		this.table=table;
		return this;
	}
}