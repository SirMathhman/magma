import magma.api.result.Result;import magma.app.error.CompileError;import java.util.List;struct Divider{
	String merge(String current, String value);
	Result<List<String>, CompileError> divide(String input);
	struct Divider new(){
		struct Divider this;
		return this;
	}
}