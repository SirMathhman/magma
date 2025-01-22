import magma.api.result.Ok;import magma.api.result.Result;import magma.app.error.CompileError;import java.util.Arrays;import java.util.List;struct SimpleDivider implements Divider{
	String delimiter;
	public SimpleDivider(String delimiter);
	String merge(String current, String value);
	Result<List<String>, CompileError> divide(String input);}