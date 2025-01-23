import magma.api.result.Result;import magma.app.error.CompileError;import java.util.List;struct Divider{
	struct VTable{
		((String, String) => String) merge;
		((String) => Result<List<String>, CompileError>) divide;
	}
	struct VTable vtable;
	struct Divider new(struct VTable table){
		struct Divider this;
		this.table=table;
		return this;
	}
}