#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
struct CompileError {
	String message;
	String context;
	List<CompileError> children;
	public CompileError(){
		this();
	}
	String display(){
		temp = temp;
	}
	int maxDepth(){
		temp = temp;
	}
	String format(){
		this.children.sort();
		temp = temp;
		return this.message + ": " + this.context + joinedChildren;
	}
};