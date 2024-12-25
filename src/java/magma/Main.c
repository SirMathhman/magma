#include "magma/api/Tuple.h"
#include "magma/compile/Node.h"
#include "magma/compile/error/ApplicationError.h"
#include "magma/compile/error/JavaError.h"
#include "magma/compile/rule/DiscardRule.h"
#include "magma/compile/rule/ExactRule.h"
#include "magma/compile/rule/LazyRule.h"
#include "magma/compile/rule/LocatingSplitter.h"
#include "magma/compile/rule/NodeListRule.h"
#include "magma/compile/rule/OrRule.h"
#include "magma/compile/rule/PrefixRule.h"
#include "magma/compile/rule/Rule.h"
#include "magma/compile/rule/SplitRule.h"
#include "magma/compile/rule/StringListRule.h"
#include "magma/compile/rule/StringRule.h"
#include "magma/compile/rule/StripRule.h"
#include "magma/compile/rule/SuffixRule.h"
#include "magma/compile/rule/SymbolRule.h"
#include "magma/compile/rule/TypeRule.h"
#include "magma/compile/rule/locate/BackwardsLocator.h"
#include "magma/compile/rule/locate/FirstLocator.h"
#include "magma/compile/rule/locate/LastLocator.h"
#include "magma/compile/rule/split/StatementSplitter.h"
#include "magma/compile/rule/split/TypeSplitter.h"
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

