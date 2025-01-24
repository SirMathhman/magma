#include "./PassingStage.h"
struct PassingStage{
	Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit);
}
