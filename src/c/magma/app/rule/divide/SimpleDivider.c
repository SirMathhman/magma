import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;
import java.util.Arrays;
import java.util.List;
public struct SimpleDivider implements Divider {
	private final String delimiter;
	public SimpleDivider(String delimiter){
		this.delimiter =delimiter;
	}
	@Override
public String merge(String current, String value){
		return current+delimiter+value;
	}
	@Override
public Result<List<String>, CompileError> divide(String input){
		return new Ok<>(Arrays.stream(input.split(delimiter)).toList());
	}
}