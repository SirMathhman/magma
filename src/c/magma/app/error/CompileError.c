#include "./CompileError.h"
struct CompileError implements Error{
	String message;
	Context context;
	List<CompileError> children;
	public CompileError(String message, Context context, List<CompileError> children){
		this.message = message;
		this.context = context;
		this.children=new ArrayList<>(children);
	}
	public CompileError(String message, Context context){
		this(message, context, Collections.emptyList());
	}
	String display(){
		return format(0);
	}
	int maxDepth(){
		return 1+this.children.stream().mapToInt(CompileError::maxDepth).max().orElse(0);
	}
	String format(int depth){
		this.children.sort(Comparator.comparingInt(CompileError::maxDepth));
		var joinedChildren=IntStream.range(0, this.children.size()).mapToObj(index->addIndentation(depth, index)).collect(Collectors.joining());
		this.message + ": " + this.context.display() + joinedChildren;
	}
	String addIndentation(int depth, int index){
		var indentation="\n"+"\t".repeat(depth);
		var formattedChild=this.children.get(index).format(depth+1);
		return indentation+index+") "+formattedChild;
	}
}
