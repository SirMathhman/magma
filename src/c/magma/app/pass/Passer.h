#include "magma/api/result/Ok.h"
#include "magma/api/result/Result.h"
#include "magma/app/Node.h"
#include "magma/app/error/CompileError.h"
#include "java/util/function/Predicate.h"
struct Passer{
	Predicate<Node> by(String type){
		return ()->value.is(type);
	}
	Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit){
		return new Ok<>(unit);
	}
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
		return new Ok<>(unit);
	}
}
