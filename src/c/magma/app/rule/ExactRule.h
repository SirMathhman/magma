#include "../../../magma/api/result/Err.h"
#include "../../../magma/api/result/Ok.h"
#include "../../../magma/api/result/Result.h"
#include "../../../magma/app/MapNode.h"
#include "../../../magma/app/Node.h"
#include "../../../magma/app/error/CompileError.h"
#include "../../../magma/app/error/context/StringContext.h"
struct ExactRule(String slice) implements Rule{
	Result<Node, CompileError> parse(String input){
		if(input.equals(this.slice))return new Ok<>(new MapNode());
		var context=new StringContext(input);
		return new Err<>(new CompileError("Exact string '"+this.slice + "' was not present", context));
	}
	Result<String, CompileError> generate(Node node){
		return new Ok<>(this.slice);
	}
}
