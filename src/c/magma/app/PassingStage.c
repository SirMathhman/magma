import magma.api.result.Ok;import magma.api.result.Result;import magma.api.stream.Streams;import magma.app.error.CompileError;import java.util.ArrayList;import java.util.List;import static magma.app.lang.CommonLang.CONTENT_BEFORE_CHILD;import static magma.app.lang.CommonLang.CONTENT_CHILDREN;struct PassingStage{
	Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit);
	Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit);
	Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit);
	PassUnit<Node> formatBlock(PassUnit<Node> inner);
	Node pruneDefinition(Node definition);
	Node retypeToStruct(Node node);
	Node removePackageStatements(Node root);
	boolean filterImport(Node child);
	Predicate<Node> by(String type);
	Result<PassUnit<Node>, CompileError> passNodeLists(PassUnit<Node> unit);
	Result<PassUnit<List<Node>>, CompileError> passAndAdd(PassUnit<List<Node>> unit, Node element);
	List<Node> add(PassUnit<List<Node>> unit2, Node value);
	Result<PassUnit<Node>, CompileError> passNodes(PassUnit<Node> unit);}