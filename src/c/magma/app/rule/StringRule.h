#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
struct StringRule implements Function<Node, Result<String, CompileError>> {
	struct StringRule implements Function<Node, Result<String, CompileError>> StringRule implements Function<Node, Result<String, CompileError>>_new(){
		struct StringRule implements Function<Node, Result<String, CompileError>> this;
		return this;
	}
		String propertyKey;
	public StringRule implements Function<Node, Result<String, CompileError>>_StringRule(void* _this_){
		struct StringRule implements Function<Node, Result<String, CompileError>> this = *(struct StringRule implements Function<Node, Result<String, CompileError>>*) this;
		temp = temp;
	}
	CompileError> StringRule implements Function<Node, Result<String, CompileError>>_apply(void* _this_){
		struct StringRule implements Function<Node, Result<String, CompileError>> this = *(struct StringRule implements Function<Node, Result<String, CompileError>>*) this;
		return node.findString(this.propertyKey)
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + this.propertyKey + "' not present", new NodeContext(node))));
	}
};