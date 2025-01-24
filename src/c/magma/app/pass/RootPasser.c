import magma.api.result.Ok;import magma.api.result.Result;import magma.app.Node;import magma.app.error.CompileError;struct RootPasser implements Passer{
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
		return new Ok<>(unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")).or(Passer.by("interface")), ()->node.retype("struct")).orElse(unit));
	}
}