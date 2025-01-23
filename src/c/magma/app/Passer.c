import magma.api.result.Result;import magma.app.error.CompileError;struct Passer{
	struct VTable{
		((Any, PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) afterPass;
		((Any, PassUnit<Node>) => Result<PassUnit<Node>, CompileError>) beforePass;
	}
	struct VTable vtable;
	struct Passer new(struct VTable table){
		struct Passer this;
		this.table=table;
		return this;
	}
}