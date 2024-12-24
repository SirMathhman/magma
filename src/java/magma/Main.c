#include "magma/api/Tuple.h"
#include "magma/compile/Node.h"
#include "magma/compile/error/ApplicationError.h"
#include "magma/compile/error/JavaError.h"
#include "magma/compile/rule/DiscardRule.h"
#include "magma/compile/rule/ExactRule.h"
#include "magma/compile/rule/FirstLocator.h"
#include "magma/compile/rule/InfixSplitter.h"
#include "magma/compile/rule/LastLocator.h"
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
	void formatBefore(){}
	void formatAfter(){}
	void pass(){}
	void passNode(){}
	void passNodeLists(){}
	void modify(){}
	void writeGenerated(){}
	void createCRootRule(){}
	void createCRootMemberRule(){}
	void createStructRule(){}
	void createGroupRule(){}
	void createStructMemberRule(){}
	void createJavaRootRule(){}
	void createClassRule(){}
	void wrapInBlock(){}
	void createClassMemberRule(){}
	void createMethodRule(){}
	void createWhitespaceRule(){}
	void createNamespacedRule(){}
	void createIncludesRule(){}
}

