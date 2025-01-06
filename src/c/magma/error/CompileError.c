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
		this.message = message;
		this.context = context;
		this.children = children;
		return this;
	}
	void new(String message, String context, CompileError... errors){
		struct CompileError this;
		caller();
		return this;
	}
	void display(void* __ref__){
		struct CompileError* this = (struct CompileError*) __ref__;
		final var joined = children.stream()
                .map(CompileError::display)
                .map(value -> "\n" + value)
                .collect(Collectors.joining());
		return a + b;
	}
};