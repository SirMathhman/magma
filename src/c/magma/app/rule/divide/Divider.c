import magma.api.result.Result;
import magma.app.error.CompileError;
import java.util.List;
public struct Divider {
	((String, String) => String) merge;
	((String) => Result<List<String>, CompileError>) divide;
}