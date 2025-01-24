#include "./OrRule.h"
struct OrRule(List<Rule> rules) implements Rule{
	List<CompileError> join(Tuple<List<CompileError>, List<CompileError>> tuple){
		var left=new ArrayList<>(tuple.left());
		left.addAll(tuple.right());
		return left;
	}
	Result<Node, CompileError> parse(String value){
		return process(new StringContext(value), rule->rule.parse(value));
	}
	<R>Result<R, CompileError> process(Context context, Function<Rule, Result<R, CompileError>> mapper){
		return Streams.fromNativeList(this.rules).map(rule->wrapResultInList(mapper, rule)).foldLeft(OrRule::join).orElseGet(()->createError(context)).mapErr(errors->new CompileError("No valid rule", context, errors));
	}
	<R>Result<R, List<CompileError>> join(Result<R, List<CompileError>> first, Result<R, List<CompileError>> second){
		return first.or(()->second).mapErr(OrRule::join);
	}
	<R>Result<R, List<CompileError>> wrapResultInList(Function<Rule, Result<R, CompileError>> mapper, Rule rule){
		return mapper.apply(rule).mapErr(Collections::singletonList);
	}
	<R>Result<R, List<CompileError>> createError(Context context){
		return new Err<>(Collections.singletonList(new CompileError("No rules set", context)));
	}
	Result<String, CompileError> generate(Node node){
		return process(new NodeContext(node), rule->rule.generate(node));
	}
}
