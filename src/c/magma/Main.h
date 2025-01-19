#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
struct Main {
	struct Main new(){
		struct Main this;
		return this;
	}
		SOURCE_DIRECTORY  = temp;
		TARGET_DIRECTORY  = temp;
		DEFAULT_VALUE  = temp;
	void main(void* _this_){
		struct Main this = *(struct Main*) this;
		collect();
	}
	IOException> collect(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	Optional<ApplicationError> runWithSources(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		return Optional.empty();
	}
	Optional<ApplicationError> runWithSource(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
	}
	IOException> readStringWrapped(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	Optional<IOException> createDirectoriesWrapped(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	Optional<IOException> writeStringWrapped(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	String merge(void* _this_){
		struct Main this = *(struct Main*) this;
		return nodes.stream()
                .map(node -> node.findString(DEFAULT_VALUE).orElse(""))
                .reduce(new StringBuilder(), merger, (_, next) -> next).toString();
	}
	CompileError> compileAll(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
		return nodes;
	}
	StringBuilder mergeStatement(void* _this_){
		struct Main this = *(struct Main*) this;
		return builder.append(element);
	}
	CompileError> splitByStatements(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		advance();
		temp = temp;
		temp = temp;
	}
	void advance(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> compileRootSegment(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	CompileError> or(void* _this_){
		struct Main this = *(struct Main*) this;
		return stream.map(Main::prepare)
                .foldLeft(Supplier::get, (current, next) -> current.or(next).mapErr(Main::merge))
                .map(result -> result.mapErr(errors -> new CompileError("Invalid " + type, input, errors)))
                .orElseGet(() -> new Err<>(new CompileError("No compilers present", input)));
	}
	CompileError> compileNamespaced(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	List<CompileError> merge(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
		temp = temp;
		copy.addAll();
		return copy;
	}
	CompileError> compileToStruct(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String generateBlock(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> splitByValues(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
		temp = temp;
		advance();
		temp = temp;
	}
	CompileError> compileStructSegment(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	CompileError> compileDefinitionStatement(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String generateDefinitionStatement(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> compileInitialization(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String generateInitialization(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	CompileError> compileMethod(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> compileStatementToNode(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String generateMethod(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> compileStatementToString(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	CompileError> compileReturn(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String generateReturn(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> parseReturn(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> compileValue(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	String generateAccess(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String generateStatement(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> truncateLeft(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	String generateDefinition(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
		temp = temp;
	}
	CompileError> compileDefinition(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	boolean isSymbol(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		return true;
	}
	String generateDefinition(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> truncateRight(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	CompileError> split(void* _this_){
		struct Main this = *(struct Main*) this;
		return locator.locate(input).<Result<Tuple<String, String>, CompileError>>map(index -> {
            final var left = input.substring(0, index);
            final var right = input.substring(index + locator.length());
            final var tuple = new Tuple<>(left, right);
            return new Ok<>(tuple);
        }).orElseGet(() -> new Err<>(new CompileError("Infix '" + locator.unwrap() + "' not present", input)));
	}
	List<CompileError>>> prepare(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
};