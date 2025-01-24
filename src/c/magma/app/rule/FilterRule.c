#include "magma/api/result/Err.h"
#include "magma/api/result/Result.h"
#include "magma/app/Node.h"
#include "magma/app/error/CompileError.h"
#include "magma/app/error/context/StringContext.h"
#include "java/util/function/Predicate.h"
struct FilterRule(Predicate<String> filter, Rule childRule) implements Rule{
	Result<Node, CompileError> parse(String input){
		if(this.filter.test(input)){
			return this.childRule.parse(input);
		}
		else{
			return new Err<>(new CompileError("Filter did not apply", new StringContext(input)));
		}
	}
	Result<String, CompileError> generate(Node node){
		return this.childRule.generate(node);
	}
}
