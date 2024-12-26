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
	void main(){}
	Tuple<State, Node> formatBefore(){}
	Tuple<State, Node> formatAfter(){}
	Tuple<State, Node> pass(){}
	Tuple<State, Node> passNode(){}
	Tuple<State, Node> passNodeLists(){}
	Tuple<State, Node> modify(){}
	Optional<ApplicationError> writeGenerated(){}
}

