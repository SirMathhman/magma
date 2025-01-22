import magma.app.error.context.Context;import java.util.ArrayList;import java.util.Collections;import java.util.Comparator;import java.util.List;import java.util.stream.Collectors;import java.util.stream.IntStream;struct CompileError implements Error{
	String message;
	Context context;
	List<CompileError> children;
	public CompileError(String message, Context context, List<CompileError> children);
	public CompileError(String message, Context context);
	String display();
	int maxDepth();
	String format(int depth);
}