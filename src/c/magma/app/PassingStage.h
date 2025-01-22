import magma.api.result.Ok;import magma.api.result.Result;import magma.api.stream.Streams;import magma.app.error.CompileError;import java.util.ArrayList;import java.util.List;import static magma.app.lang.CommonLang.CONTENT_BEFORE_CHILD;struct PassingStage {Result<PassUnit<Node>, CompileError> pass(PassUnit<Node> unit){
	return beforePass(unit).flatMapValue(PassingStage::passNodes).flatMapValue(PassingStage::passNodeLists).flatMapValue(PassingStage::afterPass);}Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit){
	return new Ok<>(unit);}Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit){
	return new Ok<>(unit.filterAndMapToValue(by("root"), PassingStage::removePackageStatements).or(()->unit.filterAndMapToValue(by("class").or(by("record")).or(by("interface")), PassingStage::retypeToStruct)).or(()->unit.filterAndMapToValue(by("definition"), PassingStage::pruneDefinition)).or(()->unit.filterAndMapToValue(by("block"), PassingStage::formatBlock)).orElse(unit));}Node formatBlock(Node block){
	return block.mapNodeList("children", ()->children.stream().map(()->child.withString(CONTENT_BEFORE_CHILD, "\n\t")).toList());}Node pruneDefinition(Node definition){
	return definition.removeNodeList("annotations").removeNodeList("modifiers");}Node retypeToStruct(Node node){
	return node.retype("struct");}Node removePackageStatements(Node root){
	return root.mapNodeList("children", ()->children.stream().filter(()->!child.is("package")).filter(PassingStage::filterImport).toList());}boolean filterImport(Node child){
	if(!child.is("import"))return true;
	var namespace=child.findString("namespace").orElse("");
	return !namespace.startsWith("java.util.function");}Predicate<Node> by(String type){
	return ()->node.is(type);}Result<PassUnit<Node>, CompileError> passNodeLists(PassUnit<Node> unit){
	return unit.value().streamNodeLists().foldLeftToResult(unit, (current, tuple) -> {
            final var propertyKey = tuple.left();
            final var propertyValues = tuple.right();
            return Streams.from(propertyValues).foldLeftToResult(current.withValue(new ArrayList<>()), PassingStage::passAndAdd).mapValue(unit1 -> unit1.mapValue(node -> current.value().withNodeList(propertyKey, node)));
        });}Result<PassUnit<List<Node>>, CompileError> passAndAdd(PassUnit<List<Node>> unit, Node element){
	return pass(unit.withValue(element)).mapValue(()->result.mapValue(()->add(unit, value)));}List<Node> add(PassUnit<List<Node>> unit2, Node value){
	var copy=new ArrayList<>(unit2.value());
	copy.add(value);
	return copy;}Result<PassUnit<Node>, CompileError> passNodes(PassUnit<Node> unit){
	return unit.value().streamNodes().foldLeftToResult(unit, (current, tuple) -> {
            final var pairKey = tuple.left();
            final var pairNode = tuple.right();

            return pass(current.withValue(pairNode)).mapValue(passed -> passed.mapValue(value -> current.value().withNode(pairKey, value)));
        });}}