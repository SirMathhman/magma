#include "../../../magma/api/result/Err.h"
#include "../../../magma/api/result/Result.h"
#include "../../../magma/api/stream/Streams.h"
#include "../../../magma/app/Node.h"
#include "../../../magma/app/error/CompileError.h"
#include "../../../magma/app/error/context/Context.h"
#include "../../../magma/app/error/context/NodeContext.h"
#include "../../../magma/app/error/context/StringContext.h"
#include "../../../java/util/ArrayList.h"
#include "../../../java/util/Collections.h"
#include "../../../java/util/List.h"
#include "../../../java/util/function/Function.h"
struct OrRule(List<Rule> rules) implements Rule{
	Result<Node, CompileError> parse(String value){
		return process(new StringContext(value), ()->rule.parse(value));
	}
	<R>Result<R, CompileError> process(Context context, Function<Rule, Result<R, CompileError>> mapper){
		Streams.fromNativeList(this.rules)
                .map(rule -> mapper.apply(rule).mapErr(Collections::singletonList))
                .foldLeft((first, second) -> first.or(() -> second).mapErr(tuple -> {
                    final var left=new ArrayList<>(tuple.left());
                    left.addAll(tuple.right());
                    return left;
                }))
                .orElseGet(()->new Err<>(Collections.singletonList(new CompileError("No rules set", context)))).mapErr(errors -> new CompileError("No valid rule", context, errors));
	}
	Result<String, CompileError> generate(Node node){
		return process(new NodeContext(node), ()->rule.generate(node));
	}
}
