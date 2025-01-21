import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.error.CompileError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static magma.app.lang.CommonLang.BLOCK_AFTER_CHILDREN;
import static magma.app.lang.CommonLang.CONTENT_AFTER_CHILD;
import static magma.app.lang.CommonLang.CONTENT_BEFORE_CHILD;
import static magma.app.lang.CommonLang.STRUCT_AFTER_CHILDREN;
struct Passer {
	static Result<Tuple<State, Node>, CompileError> pass(State state, Node root){
		return beforePass(state, root).orElse(new Ok<>(new Tuple<>(state, root))).flatMapValue(passedBefore -> passNodes(passedBefore.left(), passedBefore.right()))
                .flatMapValue(passedNodes -> passNodeLists(passedNodes.left(), passedNodes.right()))
                .flatMapValue(passedNodeLists -> afterPass(passedNodeLists.left(), passedNodeLists.right()).orElse(new Ok<>(new Tuple<>(passedNodeLists.left(), passedNodeLists.right()))));
	}
	static Optional<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node){
		return removePackageStatements(state, node).or(() -> renameToStruct(state, node))
                .or(() -> renameToSlice(state, node))
                .or(() -> enterBlock(state, node));
	}
	static Optional<? extends Result<Tuple<State, Node>, CompileError>> renameToSlice(State state, Node node){
		if(node.is("array")){
			const var child=node.findNode("child").orElse(new MapNode());
			return Optional.of(new Ok<>(new Tuple<>(state, new MapNode("slice")
                    .withNode("child", child))));
		}
		return Optional.empty();
	}
	static Optional<Result<Tuple<State, Node>, CompileError>> removeAccessModifiersFromDefinitions(State state, Node node){
		if(node.is("definition")){
			const var newNode=pruneModifiers(node).mapNodeList("modifiers", modifiers -> {
                return modifiers.stream()
                        .map(child ->child.findString("value"))
                        .flatMap(Optional::stream).map(modifier ->modifier.equals("final") ? "const" : modifier).map(value -> new MapNode("modifier").withString("value", value))
                        .toList();
            });
			return Optional.of(new Ok<>(new Tuple<>(state, newNode)));
		}
		return Optional.empty();
	}
	static Node pruneModifiers(Node node){
		const var modifiers=node.findNodeList("modifiers").orElse(Collections.emptyList());
		const var newModifiers=modifiers.stream().map(modifier ->modifier.findString("value")).flatMap(Optional::stream).filter(modifier ->!modifier.equals("public") && !modifier.equals("private")).map(modifier -> new MapNode("modifier").withString("value", modifier))
                .toList();
		Node newNode;
		if(newModifiers.isEmpty()){
			newNode=node.removeNodeList("modifiers");
		}
		else {
			newNode=node.withNodeList("modifiers", newModifiers);
		}
		return newNode;
	}
	static Optional<? extends Result<Tuple<State, Node>, CompileError>> enterBlock(State state, Node node){
		if(node.is("block")){
			return Optional.of(new Ok<>(new Tuple<>(state.enter(), node)));
		}
		return Optional.empty();
	}
	static Optional<Result<Tuple<State, Node>, CompileError>> renameToStruct(State state, Node node){
		if(node.is("class") || node.is("interface") || node.is("record")){
			return Optional.of(new Ok<>(new Tuple<>(state, node.retype("struct").withString(STRUCT_AFTER_CHILDREN, "\n"))));
		}
		return Optional.empty();
	}
	static Optional<Result<Tuple<State, Node>, CompileError>> removePackageStatements(State state, Node node){
		if(!node.is("root")){
			return Optional.empty();
		}
		const var node1=node.mapNodeList("children",  children -> {
            return children.stream()
                    .filter(child ->!child.is("package"))
                    .toList();
        });
		return Optional.of(new Ok<>(new Tuple<>(state, node1)));
	}
	static Result<Tuple<State, Node>, CompileError> passNodeLists(State state, Node previous){
		return previous.streamNodeLists().foldLeftToResult(new Tuple<>(state, previous),
                        (current, tuple) -> passNodeList(current.left(), current.right(), tuple));
	}
	static Result<Tuple<State, Node>, CompileError> passNodeList(State state, Node root, Tuple<String, List<Node>> pair){
		const var propertyKey=pair.left();
		const var propertyValues=pair.right();
		return passNodeListInStream(state, propertyValues).mapValue(list -> list.mapRight(right -> root.withNodeList(propertyKey, right)));
	}
	static Result<Tuple<State, List<Node>>, CompileError> passNodeListInStream(State state, List<Node> elements){
		return Streams.from(elements).foldLeftToResult(new Tuple<>(state, new ArrayList<>()), (current, currentElement) -> {
            final var currentState = current.left();
            final var currentElements = current.right();

            return pass(currentState, currentElement).mapValue(passingResult -> {
                return passingResult.mapRight(passedElement -> {
                    final var copy = new ArrayList<>(currentElements);
                    copy.add(passedElement);
                    return copy;
                });
            });
        });
	}
	static Optional<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node){
		return removeAccessModifiersFromDefinitions(state, node).or(() -> formatRoot(state, node))
                .or(() -> formatBlock(state, node))
                .or(() -> pruneAndFormatStruct(state, node));
	}
	static Optional<Result<Tuple<State, Node>, CompileError>> formatRoot(State state, Node node){
		if(node.is("root")){
			const var newNode=node.mapNodeList("children", children -> {
                return children.stream().map(child -> child.withString(CONTENT_AFTER_CHILD, "\n"))
                        .toList();
            });
			return Optional.of(new Ok<>(new Tuple<>(state, newNode)));
		}
		return Optional.empty();
	}
	static Optional<Result<Tuple<State, Node>, CompileError>> formatBlock(State state, Node node){
		if(node.is("block")){
			return Optional.of(new Ok<>(new Tuple<>(state.exit(), formatContent(state, node))));
		}
		return Optional.empty();
	}
	static Optional<Result<Tuple<State, Node>, CompileError>> pruneAndFormatStruct(State state, Node node){
		if(node.is("struct")){
			return Optional.of(new Ok<>(new Tuple<>(state, formatContent(state, pruneModifiers(node)))));
		}
		return Optional.empty();
	}
	static Node formatContent(State state, Node node){
		return node.withString(BLOCK_AFTER_CHILDREN, "\n"+"\t".repeat(state.depth())).mapNodeList("children",  children -> {
            return children.stream()
                    .map(child ->child.withString(CONTENT_BEFORE_CHILD, "\n"+"\t".repeat(state.depth() + 1)))
                    .toList();
        });
	}
	static Result<Tuple<State, Node>, CompileError> passNodes(State state, Node root){
		return root.streamNodes().foldLeftToResult(new Tuple<>(state, root), Passer::foldNode);
	}
	static Result<Tuple<State, Node>, CompileError> foldNode(Tuple<State, Node> current, Tuple<String, Node> tuple){
		const var currentState=current.left();
		const var currentRoot=current.right();
		const var pairKey=tuple.left();
		const var pairNode=tuple.right();
		return pass(currentState, pairNode).mapValue(passed -> passed.mapRight(right -> currentRoot.withNode(pairKey, right)));
	}
}
