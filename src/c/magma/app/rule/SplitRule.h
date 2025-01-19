#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
struct SplitRule {
	struct SplitRule SplitRule_new(){
		struct SplitRule this;
		return this;
	}
	CompileError> SplitRule_split(void* _this_){
		struct SplitRule this = *(struct SplitRule*) this;
		return locator.locate(input).<Result<Tuple<String, String>, CompileError>>map(index -> {
            final var left = input.substring(0, index);
            final var right = input.substring(index + locator.length());
            final var tuple = new Tuple<String, String>(left, right);
            return new Ok<Tuple<String, String>, CompileError>(tuple);
        }).orElseGet(() -> new Err<Tuple<String, String>, CompileError>(new CompileError("Infix '" + locator.unwrap() + "' not present", new StringContext(input))));
	}
};