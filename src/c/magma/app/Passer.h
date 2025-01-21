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

 Result<Tuple<State, Node>, CompileError> pass(State state, Node root){
	return beforePass(state, root).orElse(new Ok<>(new Tuple<>(state, root))).flatMapValue(passedBefore -> passNodes(passedBefore.left(), passedBefore.right()))
                .flatMapValue(passedNodes -> passNodeLists(passedNodes.left(), passedNodes.right()))
                .flatMapValue(passedNodeLists -> afterPass(passedNodeLists.left(), passedNodeLists.right()).orElse(new Ok<>(new Tuple<>(passedNodeLists.left(), passedNodeLists.right()))));
}

 Optional<Result<Tuple<State, Node>, CompileError>> beforePass(State state, Node node){
	return removePackageStatements(state, node).or(() -> renameToStruct(state, node))
                .or(() -> renameToSlice(state, node))
                .or(() -> renameToDataAccess(state, node))
                .or(() -> renameLambdaToMethod(state, node))
                .or(() -> enterBlock(state, node));
}

 Optional<? extends Result<Tuple<State, Node>, CompileError>> renameLambdaToMethod(State state, Node node){
	if(!node.is("lambda"))return Optional.empty();
	 auto args=findArgumentValue(node).or(auto _lambda8_(){
		return findArgumentValues(node);
	}).orElse(new ArrayList<>()).stream().map(Passer.wrapUsingAutoType).toList();
	 auto value=node.findNode("child").orElse(new MapNode());
	 auto propertyValue=value.is("block") ? value : new MapNode("block").withNodeList("children", List.of(new MapNode("return").withNode("value", value)));
	 auto retyped=node.retype(METHOD_TYPE);
	 auto params=args.isEmpty() ? retyped : retyped.withNodeList("params", args);
	 auto definition=new MapNode("definition")
                .withString("name", createUniqueName()).withNode("type", createAutoType());
	 auto method=params.withNode(METHOD_CHILD, propertyValue).withNode("definition", definition);
	return Optional.of(new Ok<>(new Tuple<>(state, method)));
}

 Node wrapUsingAutoType(String name){
	return new MapNode("definition")
                .withString("name", name).withNode("type", createAutoType());
}

 Optional<List<String>> findArgumentValue(Node node){
	return node.findNode("arg").flatMap(auto _lambda9_(auto child){
		return child.findString("value");
	}).map(Collections.singletonList);
}

 Optional<List<String>> findArgumentValues(Node node){
	return node.findNodeList("args").map(auto _lambda10_(auto list){
		return list.stream().map(auto _lambda11_(auto child){
			return child.findString("value");
		}).flatMap(Optional.stream).toList();
	});
}

 String createUniqueName(){
	 auto name="_lambda"+counter+"_";
	counter++;
	return name;
}

 Node createAutoType(){
	return new MapNode("symbol").withString("value", "auto");
}

 Optional<? extends Result<Tuple<State, Node>, CompileError>> renameToDataAccess(State state, Node node){
	if(!node.is("method-access"))return Optional.empty();
	return Optional.of(new Ok<>(new Tuple<>(state, node.retype("data-access"))));
}

 Optional<? extends Result<Tuple<State, Node>, CompileError>> renameToSlice(State state, Node node){
	if(!node.is("array"))return Optional.empty();
	 auto child=node.findNode("child").orElse(new MapNode());
	 auto slice=new MapNode("slice").withNode("child", child);
	return Optional.of(new Ok<>(new Tuple<>(state, slice)));
}

 Optional<Result<Tuple<State, Node>, CompileError>> removeAccessModifiersFromDefinitions(State state, Node node){
	if(!node.is("definition"))return Optional.empty();
	 auto newNode=pruneModifiers(node).mapNodeList("modifiers", Passer.replaceFinalWithConst).mapNode("type", Passer.replaceVarWithAuto);
	return Optional.of(new Ok<>(new Tuple<>(state, newNode)));
}

 List<Node> replaceFinalWithConst(List<Node> modifiers){
	return modifiers.stream().map(auto _lambda12_(auto child){
		return child.findString("value");
	}).flatMap(Optional.stream).map(auto _lambda13_(auto modifier){
		return modifier.equals("final") ? "const" : modifier;
	}).map(value -> new MapNode("modifier").withString("value", value))
                .toList();
}

 Node replaceVarWithAuto(Node type){
	if(!type.is("symbol"))return type;
	 auto value=type.findString("value").orElse("");
	if(!value.equals("var"))return type;
	return createAutoType();
}

 Node pruneModifiers(Node node){
	 auto modifiers=node.findNodeList("modifiers").orElse(Collections.emptyList());
	 auto newModifiers=modifiers.stream().map(auto _lambda14_(auto modifier){
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

 Optional<? extends Result<Tuple<State, Node>, CompileError>> enterBlock(State state, Node node){
	if(!node.is("block"))return Optional.empty();
	return Optional.of(new Ok<>(new Tuple<>(state.enter(), node)));
}

 Optional<Result<Tuple<State, Node>, CompileError>> renameToStruct(State state, Node node){
	if(!node.is("class") && !node.is("interface") && !node.is("record"))return Optional.empty();
	return Optional.of(new Ok<>(new Tuple<>(state, node.retype("struct").withString(STRUCT_AFTER_CHILDREN, "\n"))));
}

 Optional<Result<Tuple<State, Node>, CompileError>> removePackageStatements(State state, Node node){
	if(!node.is("root")){
		return Optional.empty();
	}
	 auto node1=node.mapNodeList("children", Passer.removePackages);
	return Optional.of(new Ok<>(new Tuple<>(state, node1)));
}

 List<Node> removePackages(List<Node> children){
	return children.stream().filter(auto _lambda16_(auto child){
		return !child.is("package");
	}).toList();
}

 Result<Tuple<State, Node>, CompileError> passNodeLists(State state, Node previous){
	return previous.streamNodeLists().foldLeftToResult(new Tuple<>(state, previous),
                        (current, tuple) -> passNodeList(current.left(), current.right(), tuple));
}

 Result<Tuple<State, Node>, CompileError> passNodeList(State state, Node root, Tuple<String, List<Node>> pair){
	 auto propertyKey=pair.left();
	 auto propertyValues=pair.right();
	return passNodeListInStream(state, propertyValues).mapValue(list -> list.mapRight(right -> root.withNodeList(propertyKey, right)));
}

 Result<Tuple<State, List<Node>>, CompileError> passNodeListInStream(State state, List<Node> elements){
	return Streams.from(elements).foldLeftToResult(new Tuple<>(state, new ArrayList<>()), (current, currentElement) -> {
            final var currentState = current.left();
            final var currentElements = current.right();
            return passAndFoldElementIntoList(currentElements, currentState, currentElement);
        });
}

 Result<Tuple<State, List<Node>>, CompileError> passAndFoldElementIntoList(List<Node> elements, State currentState, Node currentElement){
	return pass(currentState, currentElement).mapValue(auto _lambda17_(auto passingResult){
		return passingResult.mapRight(auto _lambda18_(auto passedElement){
			 auto copy=new ArrayList<>(elements);
			copy.add(passedElement);
			return copy;
		});
	});
}

 Optional<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node){
	return removeAccessModifiersFromDefinitions(state, node).or(() -> formatRoot(state, node))
                .or(() -> formatBlock(state, node))
                .or(() -> pruneAndFormatStruct(state, node));
}

 Optional<Result<Tuple<State, Node>, CompileError>> formatRoot(State state, Node node){
	if(!node.is("root"))return Optional.empty();
	 auto newNode=node.mapNodeList("children", Passer.indentRootChildren);
	return Optional.of(new Ok<>(new Tuple<>(state, newNode)));
}

 List<Node> indentRootChildren(List<Node> rootChildren){
	return rootChildren.stream().flatMap(auto _lambda19_(auto child){
		if(child.is("wrap")){
			 auto groupChildren=child.findNodeList("children").orElse(new ArrayList<>());
			 auto value=child.findNode("value").orElse(new MapNode());
			 auto copy=new ArrayList<>(groupChildren);
			copy.add(value);
			return copy.stream();
		}
		else {
			return Stream.of(child);
		}
	}).map(child -> child.withString(CONTENT_AFTER_CHILD, "\n"))
                .toList();
}

 Optional<Result<Tuple<State, Node>, CompileError>> formatBlock(State state, Node node){
	if(!node.is("block"))return Optional.empty();
	return Optional.of(new Ok<>(new Tuple<>(state.exit(), formatContent(state, node))));
}

 Optional<Result<Tuple<State, Node>, CompileError>> pruneAndFormatStruct(State state, Node node){
	if(!node.is("struct"))return Optional.empty();
	 auto pruned=pruneModifiers(node);
	 auto children=pruned.findNodeList("children").orElse(new ArrayList<>());
	 auto methods=new ArrayList<Node>();
	 auto newChildren=new ArrayList<Node>();
	children.forEach(auto _lambda20_(auto child){
		if(child.is("method")){
			methods.add(child);
		}
		else {
			newChildren.add(child);
		}
	});
	 Node withNewChildren;
	if(newChildren.isEmpty()){
		withNewChildren=pruned.removeNodeList("children");
	}
	else {
		withNewChildren=pruned.withNodeList("children", newChildren);
	}
	 auto wrapped=new MapNode("wrap")
                .withNodeList("children", methods).withNode("value", withNewChildren);
	return Optional.of(new Ok<>(new Tuple<>(state, formatContent(state, wrapped))));
}

 Node formatContent(State state, Node node){
	return node.withString(BLOCK_AFTER_CHILDREN, "\n"+"\t".repeat(Math.max(state.depth() - 1, 0))).mapNodeList("children", children -> {
            return children.stream().map(auto _lambda21_(auto child){
		return child.withString(CONTENT_BEFORE_CHILD;
	}, "\n"+"\t".repeat(state.depth()))).toList();
        });
}

 Result<Tuple<State, Node>, CompileError> passNodes(State state, Node root){
	return root.streamNodes().foldLeftToResult(new Tuple<>(state, root), Passer.foldNode);
}

 Result<Tuple<State, Node>, CompileError> foldNode(Tuple<State, Node> current, Tuple<String, Node> tuple){
	 auto currentState=current.left();
	 auto currentRoot=current.right();
	 auto pairKey=tuple.left();
	 auto pairNode=tuple.right();
	return pass(currentState, pairNode).mapValue(passed -> passed.mapRight(right -> currentRoot.withNode(pairKey, right)));
}
struct Passer { int counter=0;
}
