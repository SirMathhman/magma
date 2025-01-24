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
