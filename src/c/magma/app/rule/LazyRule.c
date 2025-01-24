struct LazyRule implements Rule{
	Optional<Rule> childRule=Optional.empty();
	Result<Node, CompileError> parse(String input){
		return findChild(new StringContext(input)).flatMapValue(()->rule.parse(input));
	}
	Result<Rule, CompileError> findChild(Context context){
		if(this.childRule.isPresent()){
			return new Ok<>(this.childRule.get());
		}
		else{
			return new Err<>(new CompileError("Child rule is not set.", context));
		}
	}
	Result<String, CompileError> generate(Node node){
		return findChild(new NodeContext(node)).flatMapValue(()->rule.generate(node));
	}
	void set(Rule childRule){
		this.childRule = Optional.of(childRule);
	}
}
