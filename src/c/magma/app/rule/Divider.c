import magma.api.result.Result;
import magma.app.error.CompileError;
import java.util.List;
public interface Divider {Result<List<String>, CompileError> divide(String input);}