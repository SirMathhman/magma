import magma.api.result.Result;import magma.app.error.CompileError;import java.util.List;struct Divider{
	struct VTable{
		String (*)(void*, String, String) merge;
		Result<List<String>, CompileError> (*)(void*, String) divide;
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