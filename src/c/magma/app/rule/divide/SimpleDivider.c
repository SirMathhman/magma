import magma.api.result.Ok;import magma.api.result.Result;import magma.app.error.CompileError;import java.util.Arrays;import java.util.List;struct SimpleDivider{
	String delimiter;
	public SimpleDivider(any* _ref_, String delimiter){
		this.delimiter =delimiter;
	}
	String merge(any* _ref_, String current, String value){
		return current+delimiter+value;
	}
	Result<List<String>, CompileError> divide(any* _ref_, String input){
		return new Ok<>(Arrays.stream(input.split(delimiter)).toList());
	}
	Divider N/A(){
		return N/A.new();
	}
}