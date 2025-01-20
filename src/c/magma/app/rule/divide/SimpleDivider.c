import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;
import java.util.Arrays;
import java.util.List;
public struct SimpleDivider implements Divider {
	private final String delimiter;
	((String) => public) SimpleDivider=public SimpleDivider(String delimiter){
		this.delimiter =delimiter;
	};
	((String, String) => String) merge=String merge(String current, String value){
		return current+delimiter+value;
	};
	((String) => Result<List<String>, CompileError>) divide=Result<List<String>, CompileError> divide(String input){
		return new Ok<>(Arrays.stream(input.split(delimiter)).toList());
	};
}