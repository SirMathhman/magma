import magma.api.result.Result;import magma.app.error.CompileError;import java.util.List;struct Divider{
	struct VTable{
		((String, String) => String) merge;
		((String) => Result<List<String>, CompileError>) divide;
	}
	struct Divider new(){
		struct Divider this;
		return this;
	}
}