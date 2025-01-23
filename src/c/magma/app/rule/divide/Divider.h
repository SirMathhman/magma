import magma.api.result.Result;import magma.app.error.CompileError;import java.util.List;struct Divider{
	struct VTable{
		((void*, String, String) => String) merge;
		((void*, String) => Result<List<String>, CompileError>) divide;
	}
	Box<void*> ref;
	struct VTable vtable;
	struct Divider new(Box<void*> ref, struct VTable vtable){
		struct Divider this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}