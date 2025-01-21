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
import static magma.app.lang.CommonLang.METHOD_CHILD;
import static magma.app.lang.CommonLang.METHOD_TYPE;
import static magma.app.lang.CommonLang.STRUCT_AFTER_CHILDREN;
struct Passer {
	 int counter=0;
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
		if(node.is("lambda")){
			 auto args=node.findNode("arg").flatMap(auto _lambda8_(auto child){
				return child.findString("value");
			}).map(Collections.singletonList).or(auto _lambda9_(){
				return node.findNodeList("args").map(auto _lambda10_(auto list){
					return list.stream().map(auto _lambda11_(auto child){
						return child.findString("value");
					}).flatMap(Optional.stream).toList();
				});
			}).orElse(new ArrayList<>()).stream().map(name -> new MapNode("definition").withString("name", name).withNode("type", createAutoType()))
                    .toList();
			 auto value=node.findNode("child").orElse(new MapNode());
			 auto propertyValue=value.is("block") ? value : new MapNode("block").withNodeList("children", List.of(new MapNode("return").withNode("value", value)));
			 auto retyped=node.retype(METHOD_TYPE);
			 auto params=args.isEmpty() ? retyped : retyped.withNodeList("params", args);
			 auto method=params.withNode(METHOD_CHILD, propertyValue).withNode("definition", new MapNode("definition")
                            .withString("name", createUniqueName()).withNode("type", createAutoType()));
			return Optional.of(new Ok<>(new Tuple<>(state, method)));
		}
		return Optional.empty();
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
		if(node.is("method-access")){
			return Optional.of(new Ok<>(new Tuple<>(state, node.retype("data-access"))));
		}
		return Optional.empty();
	}
	 Optional<? extends Result<Tuple<State, Node>, CompileError>> renameToSlice(State state, Node node){
		if(node.is("array")){
			 auto child=node.findNode("child").orElse(new MapNode());
			return Optional.of(new Ok<>(new Tuple<>(state, new MapNode("slice")
                    .withNode("child", child))));
		}
		return Optional.empty();
	}
	 Optional<Result<Tuple<State, Node>, CompileError>> removeAccessModifiersFromDefinitions(State state, Node node){
		if(node.is("definition")){
			 auto newNode=pruneModifiers(node).mapNodeList("modifiers", Passer.replaceFinalWithConst).mapNode("type", Passer.replaceVarWithAuto);
			return Optional.of(new Ok<>(new Tuple<>(state, newNode)));
		}
		return Optional.empty();
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
		Node newNode;
		if(newModifiers.isEmpty()){
			newNode=node.removeNodeList("modifiers");
		}
		else {
			newNode=node.withNodeList("modifiers", newModifiers);
		}
		return newNode;
	}
	 Optional<? extends Result<Tuple<State, Node>, CompileError>> enterBlock(State state, Node node){
		if(node.is("block")){
			return Optional.of(new Ok<>(new Tuple<>(state.enter(), node)));
		}
		return Optional.empty();
	}
	 Optional<Result<Tuple<State, Node>, CompileError>> renameToStruct(State state, Node node){
		if(node.is("class") || node.is("interface") || node.is("record")){
			return Optional.of(new Ok<>(new Tuple<>(state, node.retype("struct").withString(STRUCT_AFTER_CHILDREN, "\n"))));
		}
		return Optional.empty();
	}
	 Optional<Result<Tuple<State, Node>, CompileError>> removePackageStatements(State state, Node node){
		if(!node.is("root")){
			return Optional.empty();
		}
		 auto node1=node.mapNodeList("children", auto _lambda16_(auto children){
			return children.stream().filter(auto _lambda17_(auto child){
				return !child.is("package");
			}).toList();
		});
		return Optional.of(new Ok<>(new Tuple<>(state, node1)));
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

            return pass(currentState, currentElement).mapValue(passingResult -> {
                return passingResult.mapRight(passedElement -> {
                    final var copy = new ArrayList<>(currentElements);
                    copy.add(passedElement);
                    return copy;
                });
            });
        });
	}
	 Optional<Result<Tuple<State, Node>, CompileError>> afterPass(State state, Node node){
		return removeAccessModifiersFromDefinitions(state, node).or(() -> formatRoot(state, node))
                .or(() -> formatBlock(state, node))
                .or(() -> pruneAndFormatStruct(state, node));
	}
	 Optional<Result<Tuple<State, Node>, CompileError>> formatRoot(State state, Node node){
		if(node.is("root")){
			 auto newNode=node.mapNodeList("children", children -> {
                return children.stream().map(child -> child.withString(CONTENT_AFTER_CHILD, "\n"))
                        .toList();
            });
			return Optional.of(new Ok<>(new Tuple<>(state, newNode)));
		}
		return Optional.empty();
	}
	 Optional<Result<Tuple<State, Node>, CompileError>> formatBlock(State state, Node node){
		if(node.is("block")){
			return Optional.of(new Ok<>(new Tuple<>(state.exit(), formatContent(state, node))));
		}
		return Optional.empty();
	}
	 Optional<Result<Tuple<State, Node>, CompileError>> pruneAndFormatStruct(State state, Node node){
		if(node.is("struct")){
			return Optional.of(new Ok<>(new Tuple<>(state, formatContent(state, pruneModifiers(node)))));
		}
		return Optional.empty();
	}
	 Node formatContent(State state, Node node){
		return node.withString(BLOCK_AFTER_CHILDREN, "\n"+"\t".repeat(state.depth())).mapNodeList("children", children -> {
            return children.stream().map(auto _lambda18_(auto child){
			return child.withString(CONTENT_BEFORE_CHILD;
		}, "\n"+"\t".repeat(state.depth() + 1))).toList();
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
}
