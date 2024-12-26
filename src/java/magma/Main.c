#include "magma/api/Tuple.h"
#include "magma/compile/Node.h"
#include "magma/compile/error/ApplicationError.h"
#include "magma/compile/error/JavaError.h"
#include "magma/compile/lang/CLang.h"
#include "magma/compile/lang/JavaLang.h"
#include "java/io/IOException.h"
#include "java/nio/file/Files.h"
#include "java/nio/file/Path.h"
#include "java/nio/file/Paths.h"
#include "java/util/ArrayList.h"
#include "java/util/Collections.h"
#include "java/util/List.h"
#include "java/util/Optional.h"
#include "java/util/function/BiFunction.h"
struct Main {
	void main(String[] args){}
	Tuple<State, Node> formatBefore(State state, Node node){}
	Tuple<State, Node> formatAfter(State state, Node node){}
	Tuple<State, Node> pass(State state, Node node, BiFunction<State, Node, Tuple<State, Node>> beforePass, BiFunction<State, Node, Tuple<State, Node>> afterPass){}
	Tuple<State, Node> passNode(Tuple<State, Node> current, Tuple<String, Node> entry, BiFunction<State, Node, Tuple<State, Node>> beforePass, BiFunction<State, Node, Tuple<State, Node>> afterPass){}
	Tuple<State, Node> passNodeLists(Tuple<State, Node> current, Tuple<String, List<Node>> entry, BiFunction<State, Node, Tuple<State, Node>> beforePass, BiFunction<State, Node, Tuple<State, Node>> afterPass){}
	Tuple<State, Node> modify(State state, Node node){}
	Optional<ApplicationError> writeGenerated(Path source, String generated){}
}

