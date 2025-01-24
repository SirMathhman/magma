#include "magma/api/result/Ok.h"
#include "magma/api/result/Result.h"
#include "magma/app/Node.h"
#include "magma/app/error/CompileError.h"
struct RootPasser implements Passer{
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
		return new Ok<>(unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")).or(Passer.by("interface")), ()->node.retype("struct")).or(()->unit.filterAndMapToValue(Passer.by("import"), ()->node.retype("include"))).orElse(unit));
	}
}
