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
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
#include <temp.h>
struct Main {
	struct Main Main_new(){
		struct Main this;
		return this;
	}
		SOURCE_DIRECTORY  = temp;
		TARGET_DIRECTORY  = temp;
		DEFAULT_VALUE  = temp;
	void Main_main(void* _this_){
		struct Main this = *(struct Main*) this;
		collect();
	}
	IOException> Main_collect(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	Optional<ApplicationError> Main_runWithSources(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		return Optional.empty();
	}
	Optional<ApplicationError> Main_runWithSource(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
	}
	IOException> Main_readStringWrapped(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	Optional<IOException> Main_createDirectoriesWrapped(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	Optional<IOException> Main_writeStringWrapped(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	String Main_merge(void* _this_){
		struct Main this = *(struct Main*) this;
		return nodes.stream()
                .map(node -> generateWithDefaultValue(node))
                .reduce(new StringBuilder(), merger, (_, next) -> next).toString();
	}
	CompileError> Main_compileAll(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
		return nodes;
	}
	StringBuilder Main_mergeStatement(void* _this_){
		struct Main this = *(struct Main*) this;
		return builder.append(element);
	}
	CompileError> Main_splitByStatements(void* _this_){
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
	void Main_advance(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> Main_compileRootSegment(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	CompileError> Main_or(void* _this_){
		struct Main this = *(struct Main*) this;
		return stream.map(Main::prepare)
                .foldLeft(Supplier::get, (current, next) -> current.or(next).mapErr(Main::merge))
                .map(result -> result.mapErr(errors -> new CompileError("Invalid " + type, input, errors)))
                .orElseGet(() -> new Err<>(new CompileError("No compilers present", input)));
	}
	CompileError> Main_compileNamespaced(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	List<CompileError> Main_merge(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
		temp = temp;
		copy.addAll();
		return copy;
	}
	CompileError> Main_compileToStruct(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String Main_modifyAndGenerateStruct(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
	}
	CompileError>> Main_parseOr(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError>> Main_parseSplit(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError>> Main_parseString(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError>> Main_parseDivide(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String Main_generateBlock(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> Main_splitByValues(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
		temp = temp;
		advance();
		temp = temp;
	}
	CompileError> Main_compileStructSegment(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	CompileError> Main_compileDefinitionStatement(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String Main_generateDefinitionStatement(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> Main_compileInitialization(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String Main_generateInitialization(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	CompileError> Main_compileMethod(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String Main_generateUniqueName(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> Main_compileStatementToNode(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String Main_generateMethod(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> Main_compileStatementToString(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	CompileError> Main_compileInvocation(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> Main_compileReturn(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String Main_generateReturn(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> Main_parseReturn(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> Main_compileValue(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> Main_compileSymbol(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
		temp = temp;
		return result.mapValue(Main::createDefaultNode);
	}
	CompileError> Main_compileDataAccess(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String Main_generateAccess(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String Main_generateStatement(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> Main_truncateLeft(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	String Main_generateDefinition(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
		temp = temp;
	}
	CompileError>> Main_createDefinitionRule(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	Node Main_createDefaultNode(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	String Main_generateWithDefaultValue(void* _this_){
		struct Main this = *(struct Main*) this;
		return node.findString(DEFAULT_VALUE).orElse("");
	}
	String Main_generateDefinition(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
	CompileError> Main_truncateRight(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
		temp = temp;
	}
	CompileError> Main_split(void* _this_){
		struct Main this = *(struct Main*) this;
		return locator.locate(input).<Result<Tuple<String, String>, CompileError>>map(index -> {
            final var left = input.substring(0, index);
            final var right = input.substring(index + locator.length());
            final var tuple = new Tuple<>(left, right);
            return new Ok<>(tuple);
        }).orElseGet(() -> new Err<>(new CompileError("Infix '" + locator.unwrap() + "' not present", input)));
	}
	List<CompileError>>> Main_prepare(void* _this_){
		struct Main this = *(struct Main*) this;
		temp = temp;
	}
};