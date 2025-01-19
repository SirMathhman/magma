#include <temp.h>
#include <temp.h>
#include <temp.h>
struct CompileError(String message, String context, List<CompileError> children) implements Error {
	Rc_public CompileError(void* _this_){
		struct CompileError(String message, String context, List<CompileError> children) implements Error this = (struct CompileError(String message, String context, List<CompileError> children) implements Error*) this;
	}
};