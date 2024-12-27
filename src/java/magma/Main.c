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
struct Main {
	void main(String[] args) {
		empty()
		empty()
	}
	Optional<ApplicationError> runWithInput(Path source, String input) {
		empty()
	}
	Tuple<State, Node> formatBefore(State state, Node node) {
		if (node.is("block")) {
			empty()
		}
		empty()
	}
	Tuple<State, Node> formatAfter(State state, Node node) {
		if (node.is("group")) {
			empty()
			empty()
			empty()
			empty()
			while (i < orElse.size()) {
				empty()
				empty()
				if (state.depth() == 0 && i == 0) {
					empty()
				}
				else {
					empty()
					empty()
				}
				empty()
				empty()
			}
			empty()
		}
		elseif (node.is("block")) {
			empty()
		}
		else {
			empty()
		}
	}
	Tuple<State, Node> pass(State state, Node node, BiFunction<State, Node, Tuple<State, Node>> beforePass, BiFunction<State, Node, Tuple<State, Node>> afterPass) {
		empty()
		empty()
		empty()
		empty()
	}
	Tuple<State, Node> passNode(Tuple<State, Node> current, Tuple<String, Node> entry, BiFunction<State, Node, Tuple<State, Node>> beforePass, BiFunction<State, Node, Tuple<State, Node>> afterPass) {
		empty()
		empty()
		empty()
		empty()
		empty()
	}
	Tuple<State, Node> passNodeLists(Tuple<State, Node> current, Tuple<String, List<Node>> entry, BiFunction<State, Node, Tuple<State, Node>> beforePass, BiFunction<State, Node, Tuple<State, Node>> afterPass) {
		empty()
		empty()
		empty()
		empty()
		empty()
		empty()
		empty()
		while (i < values.size()) {
			empty()
			empty()
			empty()
			empty()
			empty()
		}
		empty()
		empty()
	}
	Tuple<State, Node> modify(State state, Node node) {
		empty()
		if (node.is("group")) {
			empty()
			empty()
			empty()
		}
		elseif (node.is("class")) {
			empty()
		}
		elseif (node.is("import")) {
			empty()
		}
		elseif (node.is("method")) {
			empty()
		}
		else {
			empty()
		}
		empty()
	}
	Optional<ApplicationError> writeGenerated(String generated, Path target) {
		empty()
	}
}

