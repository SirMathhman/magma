import magma.api.result.Ok;import magma.api.result.Result;import magma.app.error.CompileError;import java.util.Arrays;import java.util.List;import java.util.regex.Pattern;struct SimpleDivider implements Divider{
	String delimiter;
	public SimpleDivider(String delimiter){
		this.delimiter =delimiter;
	}
	String merge(String current, String value){
		return current+this.delimiter + value;
	}
	Result<List<String>, CompileError> divide(String input){
		return new Ok<>(Arrays.stream(input.split(Pattern.quote(this.delimiter))).toList());
	}
}