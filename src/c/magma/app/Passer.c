import magma.api.Tuple;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.error.CompileError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import static magma.app.lang.CommonLang.BLOCK_AFTER_CHILDREN;
import static magma.app.lang.CommonLang.CONTENT_AFTER_CHILD;
import static magma.app.lang.CommonLang.CONTENT_BEFORE_CHILD;
import static magma.app.lang.CommonLang.METHOD_CHILD;
import static magma.app.lang.CommonLang.METHOD_TYPE;
import static magma.app.lang.CommonLang.STRUCT_AFTER_CHILDREN;

static Result<Tuple<State, Node>, CompileError> pass(State state, Node root){
	return beforePass(state, root).orElse(new Ok<>(new Tuple<>(state, root))).flatMapValue(passedBefore -> passNodes(passedBefore.left(), passedBefore.right()))
                .flatMapValue(passedNodes -> passNodeLists(passedNodes.left(), passedNodes.right()))
                .flatMapValue(passedNodeLists -> afterPass(passedNodeLists.left(), passedNodeLists.right()).orElse(new Ok<>(new Tuple<>(passedNodeLists.left(), passedNodeLists.right()))));
}

static Optional<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node){
	return removePackageStatements(state, node).or(() -> renameToStruct(state, node))
                .or(() -> renameToSlice(state, node))
                .or(() -> renameToDataAccess(state, node))
                .or(() -> renameLambdaToMethod(state, node))
                .or(() -> enterBlock(state, node));
}

static Optional<? extends Result<Tuple<State, Node>, CompileError>> renameLambdaToMethod(State state, Node node){
	if(!node.is("lambda"))return Optional.empty();
	const auto args=findArgumentValue(node).or(auto _lambda8_(){
		return findArgumentValues(node);
	}).orElse(new ArrayList<>()).stream().map(Passer.wrapUsingAutoType).toList();
	const auto value=node.findNode("child").orElse(new MapNode());
	const auto propertyValue=value.is("block") ? value : new MapNode("block").withNodeList("children", List.of(new MapNode("return").withNode("value", value)));
	const auto retyped=node.retype(METHOD_TYPE);
	const auto params=args.isEmpty() ? retyped : retyped.withNodeList("params", args);
	const auto definition=new MapNode("definition")
                .withString("name", createUniqueName()).withNode("type", createAutoType());
	const auto method=params.withNode(METHOD_CHILD, propertyValue).withNode("definition", definition);
	return Optional.of(new Ok<>(new Tuple<>(state, method)));
}

static Node wrapUsingAutoType(String name){
	return new MapNode("definition")
                .withString("name", name).withNode("type", createAutoType());
}

static Optional<List<String>> findArgumentValue(Node node){
	return node.findNode("arg").flatMap(auto _lambda9_(auto child){
		return child.findString("value");
	}).map(Collections.singletonList);
}

static Optional<List<String>> findArgumentValues(Node node){
	return node.findNodeList("args").map(auto _lambda10_(auto list){
		return list.stream().map(auto _lambda11_(auto child){
			return child.findString("value");
		}).flatMap(Optional.stream).toList();
	});
}

static String createUniqueName(){
	const auto name="_lambda"+counter+"_";
	counter++;
	return name;
}

static Node createAutoType(){
	return new MapNode("symbol").withString("value", "auto");
}

static Optional<? extends Result<Tuple<State, Node>, CompileError>> renameToDataAccess(State state, Node node){
	if(!node.is("method-access"))return Optional.empty();
	return Optional.of(new Ok<>(new Tuple<>(state, node.retype("data-access"))));
}

static Optional<? extends Result<Tuple<State, Node>, CompileError>> renameToSlice(State state, Node node){
	if(!node.is("array"))return Optional.empty();
	const auto child=node.findNode("child").orElse(new MapNode());
	const auto slice=new MapNode("slice").withNode("child", child);
	return Optional.of(new Ok<>(new Tuple<>(state, slice)));
}

static Optional<Result<Tuple<State, Node>, CompileError>> removeAccessModifiersFromDefinitions(State state, Node node){
	if(!node.is("definition"))return Optional.empty();
	const auto newNode=pruneModifiers(node).mapNodeList("modifiers", Passer.replaceFinalWithConst).mapNode("type", Passer.replaceVarWithAuto);
	return Optional.of(new Ok<>(new Tuple<>(state, newNode)));
}

static List<Node> replaceFinalWithConst(List<Node> modifiers){
	return modifiers.stream().map(auto _lambda12_(auto child){
		return child.findString("value");
	}).flatMap(Optional.stream).map(auto _lambda13_(auto modifier){
		return modifier.equals("final") ? "const" : modifier;
	}).map(value -> new MapNode("modifier").withString("value", value))
                .toList();
}

static Node replaceVarWithAuto(Node type){
	if(!type.is("symbol"))return type;
	const auto value=type.findString("value").orElse("");
	if(!value.equals("var"))return type;
	return createAutoType();
}

static Node pruneModifiers(Node node){
	const auto modifiers=node.findNodeList("modifiers").orElse(Collections.emptyList());
	const auto newModifiers=modifiers.stream().map(auto _lambda14_(auto modifier){
		return modifier.findString("value");
	}).flatMap(Optional.stream).filter(auto _lambda15_(auto modifier){
		return !modifier.equals("public") && !modifier.equals("private");
	}).map(modifier -> new MapNode("modifier").withString("value", modifier))
                .toList();
	if(newModifiers.isEmpty()){
		return node.removeNodeList("modifiers");
	}
	else {
		return node.withNodeList("modifiers", newModifiers);
	}
}

static Optional<? extends Result<Tuple<State, Node>, CompileError>> enterBlock(State state, Node node){
	if(!node.is("block"))return Optional.empty();
	return Optional.of(new Ok<>(new Tuple<>(state.enter(), node)));
}

static Optional<Result<Tuple<State, Node>, CompileError>> renameToStruct(State state, Node node){
	if(!node.is("class") && !node.is("interface") && !node.is("record"))return Optional.empty();
	return Optional.of(new Ok<>(new Tuple<>(state, node.retype("struct").withString(STRUCT_AFTER_CHILDREN, "\n"))));
}

static Optional<Result<Tuple<State, Node>, CompileError>> removePackageStatements(State state, Node node){
	if(!node.is("root")){
		return Optional.empty();
	}
	const auto node1=node.mapNodeList("children", Passer.removePackages);
	return Optional.of(new Ok<>(new Tuple<>(state, node1)));
}

static List<Node> removePackages(List<Node> children){
	return children.stream().filter(auto _lambda16_(auto child){
		return !child.is("package");
	}).toList();
}

static Result<Tuple<State, Node>, CompileError> passNodeLists(State state, Node previous){
	return previous.streamNodeLists().foldLeftToResult(new Tuple<>(state, previous),
                        (current, tuple) -> passNodeList(current.left(), current.right(), tuple));
}

static Result<Tuple<State, Node>, CompileError> passNodeList(State state, Node root, Tuple<String, List<Node>> pair){
	const auto propertyKey=pair.left();
	const auto propertyValues=pair.right();
	return passNodeListInStream(state, propertyValues).mapValue(list -> list.mapRight(right -> root.withNodeList(propertyKey, right)));
}

static Result<Tuple<State, List<Node>>, CompileError> passNodeListInStream(State state, List<Node> elements){
	return Streams.from(elements).foldLeftToResult(new Tuple<>(state, new ArrayList<>()), (current, currentElement) -> {
            final var currentState = current.left();
            final var currentElements = current.right();
            return passAndFoldElementIntoList(currentElements, currentState, currentElement);
        });
}

static Result<Tuple<State, List<Node>>, CompileError> passAndFoldElementIntoList(List<Node> elements, State currentState, Node currentElement){
	return pass(currentState, currentElement).mapValue(auto _lambda17_(auto passingResult){
		return passingResult.mapRight(auto _lambda18_(auto passedElement){
			const auto copy=new ArrayList<>(elements);
			copy.add(passedElement);
			return copy;
		});
	});
}

static Optional<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node){
	return removeAccessModifiersFromDefinitions(state, node).or(() -> formatRoot(state, node))
                .or(() -> formatBlock(state, node))
                .or(() -> pruneAndFormatStruct(state, node))
                .or(() -> pruneFunction(state, node));
}

static Optional<Result<Tuple<State, Node>, CompileError>> pruneFunction(State state, Node node){
	if(node.is("method")){
		const auto node1=node.mapNode("definition", auto _lambda19_(auto definition){
			return definition.removeNodeList("annotations");
		});
		return Optional.of(new Ok<>(new Tuple<>(state, node1)));
	}
	return Optional.empty();
}

static Optional<Result<Tuple<State, Node>, CompileError>> formatRoot(State state, Node node){
	if(!node.is("root"))return Optional.empty();
	const auto newNode=node.mapNodeList("children", Passer.indentRootChildren);
	return Optional.of(new Ok<>(new Tuple<>(state, newNode)));
}

static List<Node> indentRootChildren(List<Node> rootChildren){
	return rootChildren.stream().flatMap(Passer.flattenWrap).map(child -> child.withString(CONTENT_AFTER_CHILD, "\n"))
                .toList();
}

static Stream<Node> flattenWrap(Node child){
	if(!child.is("wrap"))return Stream.of(child);
	const auto groupChildren=child.findNodeList("children").orElse(new ArrayList<>());
	const auto value=child.findNode("value").orElse(new MapNode());
	const auto copy=new ArrayList<>(groupChildren);
	copy.add(value);
	return copy.stream();
}

static Optional<Result<Tuple<State, Node>, CompileError>> formatBlock(State state, Node node){
	if(!node.is("block"))return Optional.empty();
	return Optional.of(new Ok<>(new Tuple<>(state.exit(), formatContent(state, node))));
}

static Optional<Result<Tuple<State, Node>, CompileError>> pruneAndFormatStruct(State state, Node node){
	if(!node.is("struct"))return Optional.empty();
	const auto pruned=pruneModifiers(node);
	const auto children=pruned.findNodeList("children").orElse(new ArrayList<>());
	const auto methods=new ArrayList<Node>();
	const auto newChildren=new ArrayList<Node>();
	children.forEach(auto _lambda20_(auto child){
		if(child.is("method")){
			methods.add(child);
		}
		else {
			newChildren.add(child);
		}
	});
	const Node withNewChildren;
	if(newChildren.isEmpty()){
		withNewChildren=pruned.removeNodeList("children");
	}
	else {
		withNewChildren=pruned.withNodeList("children", newChildren);
	}
	const auto wrapped=new MapNode("wrap")
                .withNodeList("children", methods).withNode("value", withNewChildren);
	return Optional.of(new Ok<>(new Tuple<>(state, formatContent(state, wrapped))));
}

static Node formatContent(State state, Node node){
	return node.withString(BLOCK_AFTER_CHILDREN, "\n"+"\t".repeat(Math.max(state.depth() - 1, 0))).mapNodeList("children", children -> {
            return children.stream().map(auto _lambda21_(auto child){
		return child.withString(CONTENT_BEFORE_CHILD;
	}, "\n"+"\t".repeat(state.depth()))).toList();
        });
}

static Result<Tuple<State, Node>, CompileError> passNodes(State state, Node root){
	return root.streamNodes().foldLeftToResult(new Tuple<>(state, root), Passer.foldNode);
}

static Result<Tuple<State, Node>, CompileError> foldNode(Tuple<State, Node> current, Tuple<String, Node> tuple){
	const auto currentState=current.left();
	const auto currentRoot=current.right();
	const auto pairKey=tuple.left();
	const auto pairNode=tuple.right();
	return pass(currentState, pairNode).mapValue(passed -> passed.mapRight(right -> currentRoot.withNode(pairKey, right)));
}
struct Passer {static int counter=0;
}
