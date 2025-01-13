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
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
struct Main {
	Path SOURCE_DIRECTORY = temp();
	Path TARGET_DIRECTORY = temp();
	void main(String[] args){
		temp();
	}
	Optional<IOException> compileFiles(List<Path> files){
		return temp();
	}
	Optional<IOException> compileSource(Path source){
		auto relativized = temp();
		auto parent = temp();
		auto namespace = temp();
		if (1) {}
		auto name = temp();
		auto nameWithoutExt = temp();
		auto targetParent = temp();
		if (1) {}
		auto target = temp();
		return temp();
	}
	List<String> computeNamespace(Path parent){
		return temp();
	}
	String compileRoot(String root){
		return temp();
	}
	String compileAndMerge(List<String> segments, (String => String) compiler, ((StringBuilder, String) => StringBuilder) merger){
		return temp();
	}
	String merge(List<String> segments, ((StringBuilder, String) => StringBuilder) merger){
		return temp();
	}
	List<String> compileSegments(List<String> segments, (String => String) compiler){
		return temp();
	}
	List<String> slicesOf(((State, Character) => State) other, String root){
		auto queue = temp();
		auto state = temp();
		while (1) {}
		auto segments = temp().segments;
		if (1) {}
		return temp();
	}
	State splitAtChar(State state, Character c, ((State, Character) => State) other){
		return temp();
	}
	Optional<State> splitDoubleQuotes(State state, char c){
		if (1) {}
		auto current = temp();
		while (1) {}
		return temp();
	}
	Optional<State> splitDoubleQuotesChar(State state){
		auto maybeNext = temp();
		if (1) {}
		auto nextTuple = temp();
		auto nextChar = temp();
		if (1) {}
		if (1) {}
		else {}
	}
	State statementChars(State state, char c){
		auto appended = temp();
		if (1) {}
		if (1) {}
		if (1) {}
		if (1) {}
		return appended;
	}
	Optional<State> splitSingleQuotes(State state, char c){
		if (1) {}
		return temp();
	}
	String compileRootSegment(String rootSegment){
		if (1) {}
		if (1) {}
		return temp();
	}
	String invalidate(String type, String rootSegment){
		return temp();
	}
	T writeError(PrintStream stream, String message, String rootSegment, T value){
		temp();
		return value;
	}
	Optional<String> compileToStruct(String keyword, String rootSegment){
		return temp();
	}
	String compileStructSegment(String structSegment){
		return temp();
	}
	Optional<String> compileDefinitionStatement(String structSegment, int depth){
		return temp();
	}
	Optional<String> compileDefinition(String definition){
		return temp();
	}
	Optional<String> compileType(String type){
		auto optional = temp();
		if (1) {}
		return temp();
	}
	Optional<String> compileExact(String type, String match, String output){
		return temp();
	}
	Optional<String> writeDebug(String type){
		temp();
		return temp();
	}
	Optional<String> compileArray(String type){
		return temp();
	}
	Optional<String> compileGeneric(String type){
		return temp();
	}
	StringBuilder mergeValues(StringBuilder builder, String slice){
		if (1) {}
		return temp();
	}
	State valueStrings(State state, Character c){
		if (1) {}
		auto appended = temp();
		if (1) {}
		if (1) {}
		return appended;
	}
	Optional<String> compileFilter(Predicate<String> filter, String type){
		return temp();
	}
	boolean isSymbol(String type){
		return temp();
	}
	String generateDefinition(String type, String name){
		return type + " " + name;
	}
	String generateStatement(int depth, String content){
		return "\n" + "\t".repeat(depth) + content + ";";
	}
	Optional<String> compileMethod(String structSegment){
		return temp();
	}
	Optional<String> compileContent(String maybeContent){
		return temp();
	}
	String compileStatement(String statement, int depth){
		return temp();
	}
	Optional<String> compileElse(String statement){
		return temp();
	}
	Optional<String> compileCondition(String statement, String prefix){
		return temp();
	}
	Optional<String> compileInvocationStatement(String input, int depth){
		return temp();
	}
	Optional<String> compileInvocation(String input){
		return temp();
	}
	String generateInvocation(){
		return "temp()";
	}
	Optional<String> compileReturn(String statement, int depth){
		return temp();
	}
	Optional<String> truncateLeft(String input, String slice){
		return temp();
	}
	Optional<String> compileAssignment(String statement, int depth){
		return temp();
	}
	String compileValue(String value){
		return temp();
	}
	Optional<String> compileString(String value){
		return temp();
	}
	Optional<String> compileAdd(String value){
		return temp();
	}
	boolean isNumber(String input){
		return temp();
	}
	Optional<String> compileConstruction(String value){
		return temp();
	}
	Optional<String> compileDataAccess(String value){
		return temp();
	}
	Optional<String> compileInitialization(String structSegment, int depth){
		return temp();
	}
	Optional<String> truncateRight(String input, String slice){
		if (1) {}
		return temp();
	}
	Optional<Tuple<String, String>> split(String input, Locator locator){
		return temp();
	}
};