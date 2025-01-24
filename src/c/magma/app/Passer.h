import magma.api.result.Result;import magma.app.error.CompileError;struct Passer{
	Predicate<Node> by(any* _ref_, String type){
		return ()->value.is(type);
	}
	Result<PassUnit<Node>, CompileError> afterPass(any* _ref_, PassUnit<Node> unit);
	Result<PassUnit<Node>, CompileError> beforePass(any* _ref_, PassUnit<Node> unit);
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}