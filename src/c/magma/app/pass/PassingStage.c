#include "../../../magma/api/result/Result.h"
#include "../../../magma/app/Node.h"
#include "../../../magma/app/error/CompileError.h"
struct PassingStage{
	Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit);
}
