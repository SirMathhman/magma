#include "../../../magma/app/error/context/Context.h"
#include "../../../java/util/ArrayList.h"
#include "../../../java/util/Collections.h"
#include "../../../java/util/Comparator.h"
#include "../../../java/util/List.h"
#include "../../../java/util/stream/Collectors.h"
#include "../../../java/util/stream/IntStream.h"
struct CompileError implements Error{
	String message;
	Context context;
	List<CompileError> children;
	public CompileError(String message, Context context, List<CompileError> children){
		this.message =message;
		this.context =context;
		this.children = new ArrayList<>(children);
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
		var joinedChildren=IntStream.range(0, this.children.size()).mapToObj(index -> "\n" + "\t".repeat(depth) + index + ") " + this.children.get(index).format(depth + 1))
                .collect(Collectors.joining());
		return this.message + ": " + this.context.display() + joinedChildren;
	}
}
