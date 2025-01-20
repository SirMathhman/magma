import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;
import magma.app.rule.Splitter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
public struct StatementDivider implements Divider {
	public static final Divider STATEMENT_DIVIDER = new StatementDivider();
	private StatementDivider();
	@Override    public String merge(String current,  String value){
		return current+value;
	}
	@Override    public Result<List<String>, CompileError> divide(String input);
}