#include "magma/api/Tuple.h"
#include "magma/compile/Node.h"
#include "magma/compile/error/ApplicationError.h"
#include "magma/compile/error/JavaError.h"
#include "magma/compile/rule/DiscardRule.h"
#include "magma/compile/rule/ExactRule.h"
#include "magma/compile/rule/LazyRule.h"
#include "magma/compile/rule/split/LocatingSplitter.h"
#include "magma/compile/rule/slice/NodeListRule.h"
#include "magma/compile/rule/OrRule.h"
#include "magma/compile/rule/string/PrefixRule.h"
#include "magma/compile/rule/Rule.h"
#include "magma/compile/rule/split/SplitRule.h"
#include "magma/compile/rule/string/StringListRule.h"
#include "magma/compile/rule/string/StringRule.h"
#include "magma/compile/rule/string/StripRule.h"
#include "magma/compile/rule/string/SuffixRule.h"
#include "magma/compile/rule/string/SymbolRule.h"
#include "magma/compile/rule/TypeRule.h"
#include "magma/compile/rule/split/locate/BackwardsLocator.h"
#include "magma/compile/rule/split/locate/FirstLocator.h"
#include "magma/compile/rule/split/locate/LastLocator.h"
#include "magma/compile/rule/slice/StatementSlicer.h"
#include "magma/compile/rule/slice/TypeSlicer.h"
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
	Rule createCRootRule(){}
	OrRule createCRootMemberRule(){}
	Rule createStructRule(){}
	Rule createGroupRule(){}
	Rule createStructMemberRule(){}
	TypeRule createFunctionRule(){}
	Rule createJavaRootRule(){}
	TypeRule createClassRule(){}
	SplitRule wrapInBlock(){}
	Rule createClassMemberRule(){}
	TypeRule createMethodRule(){}
	Rule createTypeRule(){}
	TypeRule createSymbolRule(){}
	TypeRule createGenericRule(){}
	TypeRule createWhitespaceRule(){}
	Rule createNamespacedRule(){}
	Rule createIncludesRule(){}
}

