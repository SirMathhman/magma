#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
struct Main {
	void main(String[] args){}
	Result<String, CompileException> compile(String root){}
	Result<String, CompileException> splitAndCompile(String root, Function<String, Result<String, CompileException>> compiler){}
	Result<List<String>, CompileException> split(String input){}
	State splitAtChar(State state, char c, Deque<Character> queue){}
	Result<String, CompileException> compileRootMember(String rootSegment){}
	Optional<Result<String, CompileException>> compileClass(String rootSegment){}
	Optional<String> truncateRight(String input, String slice){}
	Result<String, CompileException> compileClassStatement(String classMember){}
	Optional<Result<String, CompileException>> compileMethod(String classMember){}
	Optional<Tuple<String, String>> split(String withParams, String s){}
	Optional<Integer> locateTypeSeparator(String input, String slice){}
	Optional<Integer> locateLast(String input, String slice){}
	Optional<Tuple<String, String>> split(String input, String slice, BiFunction<String, String, Optional<Integer>> locator){}
	Optional<Integer> locateFirst(String input, String slice){}
};