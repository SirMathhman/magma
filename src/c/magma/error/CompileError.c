#include "temp.h";
#include "temp.h";
#include "temp.h";
struct CompileError {
	String message;
	String context;
	List<CompileError> children;
	void new(String message, String context){
		struct CompileError this;
		caller();
		return this;
	}
	void new(String message, String context, List<CompileError> children){
		struct CompileError this;
		this.message = Node[value=message];
		this.context = Node[value=context];
		this.children = Node[value=children];
		return this;
	}
	void new(String message, String context, CompileError... errors){
		struct CompileError this;
		caller();
		return this;
	}
	void display(void* __ref__){
		struct CompileError this = *(struct CompileError*) __ref__;
		final var joined = Node[value=Node[value=Node[value=this].children.stream()
                .map(CompileError::display)
                .map(value -> "\n" + value)
                .collect](Node[value=Node[value=Node[value=Collectors].joining](Node[value=])])];
		return Node[value=Node[value=this].message + ": " + this.context + joined];
	}
};