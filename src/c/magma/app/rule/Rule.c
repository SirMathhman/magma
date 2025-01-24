struct Rule{
	Result<Node, CompileError> parse(String input);
	Result<String, CompileError> generate(Node node);
}
