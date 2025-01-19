#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
struct CompileError {
	String message;
	String context;
	List<CompileError> children;
	struct CompileError new(String message, String context, List<CompileError> children){
		struct CompileError this;
		this.message = message;
		this.context = context;
		this.children = children;
		return this;
	}
	public CompileError(void* _this_){
		struct CompileError this = *(struct CompileError*) this;
		this();
	}
	String display(void* _this_){
		struct CompileError this = *(struct CompileError*) this;
		temp = temp;
	}
	int maxDepth(void* _this_){
		struct CompileError this = *(struct CompileError*) this;
		temp = temp;
	}
	String format(void* _this_){
		struct CompileError this = *(struct CompileError*) this;
		this.children.sort();
		temp = temp;
		return this.message + ": " + this.context + joinedChildren;
	}
};