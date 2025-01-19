#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
struct CompileError {
	String message;
	String context;
	List<CompileError> children;
	struct CompileError CompileError_new(String message, String context, List<CompileError> children){
		struct CompileError this;
		this.message = message;
		this.context = context;
		this.children = children;
		return this;
	}
	public CompileError_new(void* _this_){
		struct CompileError this = *(struct CompileError*) this;
		this();
	}
	String CompileError_display(void* _this_){
		struct CompileError this = *(struct CompileError*) this;
		temp = temp;
	}
	int CompileError_maxDepth(void* _this_){
		struct CompileError this = *(struct CompileError*) this;
		temp = temp;
	}
	String CompileError_format(void* _this_){
		struct CompileError this = *(struct CompileError*) this;
		this.children.sort();
		temp = temp;
		return this.message + ": " + this.context + joinedChildren;
	}
};