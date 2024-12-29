#include "magma/api/JavaFiles.h"
#include "magma/api/Tuple.h"
#include "magma/api/result/Err.h"
#include "magma/api/result/Ok.h"
#include "magma/api/result/Result.h"
#include "magma/compile/Node.h"
#include "magma/compile/error/ApplicationError.h"
#include "magma/compile/error/JavaError.h"
#include "magma/compile/lang/CLang.h"
#include "magma/compile/lang/JavaLang.h"
#include "java/nio/file/Path.h"
#include "java/nio/file/Paths.h"
#include "java/util/ArrayList.h"
#include "java/util/Collections.h"
#include "java/util/List.h"
#include "java/util/Optional.h"
#include "java/util/function/BiFunction.h"
#include "java/util/stream/Collectors.h"
struct Main{
	void main(String[] args){
		final Path source=Paths.get(".", "src", "java", "magma", "Main.java");
		empty()
	}
	Optional<ApplicationError> runWithInput(Path source, String input){
		return JavaLang.createJavaRootRule().parse(input).mapErr(ApplicationError::new).flatMapValue(()->writeInputAST(source, parsed)).mapValue(()->pass(new State(), node, Tuple::new, Main::modify).right()).mapValue(()->pass(new State(), node, Main::formatBefore, Main::formatAfter).right()).flatMapValue(()->CLang.createCRootRule().generate(parsed).mapErr(ApplicationError::new)).mapValue(()->writeGenerated(generated, source.resolveSibling("Main.c"))).match(()->value, Optional::of);
	}
	Result<Node, ApplicationError> writeInputAST(Path source, Node parsed){
		return JavaFiles.writeString(source.resolveSibling("Main.input.ast"), parsed.toString()).map(JavaError::new).map(ApplicationError::new).<Result<Node, ApplicationError>>map(Err::new).orElseGet(()->new Ok(parsed));
	}
	Tuple<State, Node> formatBefore(State state, Node node){
		if (node.is("block")){
			return new Tuple(state.enter(), node);
		}
		return new Tuple(state, node);
	}
	Tuple<State, Node> formatAfter(State state, Node node){
		if (node.is("group")){
			final var oldChildren=node.findNodeList("children");
			final var newChildren = new ArrayList<Node>();
			List<Node> orElse=oldChildren.orElse(Collections.emptyList());
			int i=0;
			while (i<orElse.size()){
				Node child=orElse.get(i);
				final var withString=getNode(state, i, child);
				empty()
				i = i + 1;
			}
			return new Tuple(state, node.withNodeList("children", newChildren).withString("after-children", "\n" + "\t".repeat(Math.max(state.depth()-1, 0))));
		}
		else if (node.is("block")){
			return new Tuple(state.exit(), node);
		}
		else {
			return new Tuple(state, node);
		}
	}
	Node getNode(State state, int i, Node child){
		if (state.depth() == 0 && i == 0) return child;
		final var indent="\n" + "\t".repeat(state.depth());
		return child.withString("before-child", indent);
	}
	Tuple<State, Node> pass(State state, Node node, BiFunction<State, Node, Tuple<State, Node>> beforePass, BiFunction<State, Node, Tuple<State, Node>> afterPass){
		final var withBefore=beforePass.apply(state, node);
		final var withNodeLists = withBefore.right()
                .streamNodeLists()
                .reduce(withBefore, (node1, tuple) -> passNodeLists(node1, tuple, beforePass, afterPass), (_, next) -> next);
		final var withNodes = withNodeLists.right()
                .streamNodes()
                .reduce(withNodeLists, (node1, tuple) -> passNode(node1, tuple, beforePass, afterPass), (_, next) -> next);
		return afterPass.apply(withNodes.left(), withNodes.right());
	}
	Tuple<State, Node> passNode(Tuple<State, Node> current, Tuple<String, Node> entry, BiFunction<State, Node, Tuple<State, Node>> beforePass, BiFunction<State, Node, Tuple<State, Node>> afterPass){
		final var oldState=current.left();
		final var oldNode=current.right();
		final var key=entry.left();
		final var value=entry.right();
		return pass(oldState, value, beforePass, afterPass).mapRight(()->oldNode.withNode(key, right));
	}
	Tuple<State, Node> passNodeLists(Tuple<State, Node> current, Tuple<String, List<Node>> entry, BiFunction<State, Node, Tuple<State, Node>> beforePass, BiFunction<State, Node, Tuple<State, Node>> afterPass){
		final var oldState=current.left();
		final var oldChildren=current.right();
		final var key=entry.left();
		final var values=entry.right();
		var currentState=oldState;
		var currentChildren = new ArrayList<Node>();
		int i=0;
		while (i<values.size()){
			Node value=values.get(i);
			final var passed=pass(currentState, value, beforePass, afterPass);
			currentState = passed.left();
			empty()
			i = i + 1;
		}
		final var newNode=oldChildren.withNodeList(key, currentChildren);
		return new Tuple(oldState, newNode);
	}
	Tuple<State, Node> modify(State state, Node node){
		final var result=modifyStateless(node);
		return new Tuple(state, result);
	}
	Node modifyStateless(Node node){
		if (node.is("group")){
			final var oldChildren=node.findNodeList("children").orElse(new ArrayList());
			final var newChildren = oldChildren.stream()
                    .filter(oldChild -> !oldChild.is("package"))
                    .collect(Collectors.toCollection(ArrayList::new));
			return node.withNodeList("children", newChildren);
		}
		else if (node.is("class")){
			return node.retype("struct");
		}
		else if (node.is("import")){
			return node.retype("include");
		}
		else if (node.is("method")){
			return node.retype("function");
		}
		else {
			return node;
		}
	}
	Optional<ApplicationError> writeGenerated(String generated, Path target){
		return JavaFiles.writeString(target, generated).map(JavaError::new).map(ApplicationError::new);
	}
}

