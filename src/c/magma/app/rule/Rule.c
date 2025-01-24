#include "../../../magma/api/result/Result.h"
#include "../../../magma/app/Node.h"
#include "../../../magma/app/error/CompileError.h"
struct Rule{
	Result<Node, CompileError> parse(String input);
	Result<String, CompileError> generate(Node node);
}
