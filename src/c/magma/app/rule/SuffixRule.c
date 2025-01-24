struct SuffixRule implements Rule{
	String suffix;
	Rule childRule;
	public SuffixRule(Rule childRule, String suffix){
		this.suffix = suffix;
		this.childRule = childRule;
	}
	Result<String, CompileError> truncateRight(String input, String slice){
		if(input.endsWith(slice)){
			return new Ok<>(input.substring(0, input.length() - slice.length()));
		}
		else{
			return new Err<>(new CompileError("Suffix '"+slice+"' not present", new StringContext(input)));
		}
	}
	Result<Node, CompileError> parse(String input){
		return truncateRight(input, this.suffix).flatMapValue(this.childRule::parse);
	}
	Result<String, CompileError> generate(Node node){
		return childRule.generate(node).mapValue(()->inner+suffix);
	}
}
