import magma.api.result.Result;import magma.app.error.CompileError;import java.util.List;struct Divider{
	struct VTable{
		((Any, String, String) => String) merge;
		((Any, String) => Result<List<String>, CompileError>) divide;
	}
	Box<Any> ref;
	struct VTable vtable;
	struct Divider new(Box<Any> ref, struct VTable vtable){
		struct Divider this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}