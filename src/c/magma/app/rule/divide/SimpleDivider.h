import magma.api.result.Ok;import magma.api.result.Result;import magma.app.error.CompileError;import java.util.Arrays;import java.util.List;struct SimpleDivider implements Divider{
	String delimiter;
	public SimpleDivider(String delimiter){
		this.delimiter =delimiter;
	}
	String merge(String current, String value){
		return current+delimiter+value;
	}
	Result<List<String>, CompileError> divide(String input){
		return Ok<>.new();
	}struct SimpleDivider new(){struct SimpleDivider this;return this;}
}