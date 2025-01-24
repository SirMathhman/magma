import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;struct Rule{
	Result<Node, CompileError> parse(any* _ref_, String input);
	Result<String, CompileError> generate(any* _ref_, Node node);
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}