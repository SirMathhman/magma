import magma.api.result.Result;import magma.app.error.CompileError;struct Passer<Capture>{
	struct VTable{
		((PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) afterPass;
		((PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) beforePass;
	}
	struct VTable vtable;
	struct Passer new(struct VTable table){
		struct Passer this;
		this.table=table;
		return this;
	}
}