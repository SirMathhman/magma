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
	}
		SOURCE_DIRECTORY  = temp;
		TARGET_DIRECTORY  = temp;
		DEFAULT_VALUE  = temp;
	void main(){
		collect();
	}
	IOException> collect(){
		temp = temp;
		temp = temp;
	}
	Optional<ApplicationError> runWithSources(){
		temp = temp;
		return Optional.empty();
	}
	Optional<ApplicationError> runWithSource(){
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
	}
	IOException> readStringWrapped(){
		temp = temp;
		temp = temp;
	}
	Optional<IOException> createDirectoriesWrapped(){
		temp = temp;
		temp = temp;
	}
	Optional<IOException> writeStringWrapped(){
		temp = temp;
		temp = temp;
	}
	String merge(){
		return nodes.stream()
                .map(node -> node.findString(DEFAULT_VALUE).orElse(""))
                .reduce(new StringBuilder(), merger, (_, next) -> next).toString();
	}
	CompileError> compileAll(){
		temp = temp;
		temp = temp;
		return nodes;
	}
	StringBuilder mergeStatement(){
		return builder.append(element);
	}
	CompileError> splitByStatements(){
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		temp = temp;
		advance();
		temp = temp;
		temp = temp;
	}
	void advance(){
		temp = temp;
	}
	CompileError> compileRootSegment(){
		temp = temp;
		temp = temp;
	}
	CompileError> or(){
		return stream.map(Main::prepare)
                .foldLeft(Supplier::get, (current, next) -> current.or(next).mapErr(Main::merge))
                .map(result -> result.mapErr(errors -> new CompileError("Invalid " + type, input, errors)))
                .orElseGet(() -> new Err<>(new CompileError("No compilers present", input)));
	}
	CompileError> compileNamespaced(){
		temp = temp;
		temp = temp;
	}
	List<CompileError> merge(){
		temp = temp;
		temp = temp;
		temp = temp;
		copy.addAll();
		return copy;
	}
	CompileError> compileToStruct(){
		temp = temp;
	}
	String generateBlock(){
		temp = temp;
	}
	CompileError> splitByValues(){
		temp = temp;
		temp = temp;
		temp = temp;
		advance();
		temp = temp;
	}
	CompileError> compileStructSegment(){
		temp = temp;
		temp = temp;
	}
	CompileError> compileDefinitionStatement(){
		temp = temp;
	}
	String generateDefinitionStatement(){
		temp = temp;
	}
	CompileError> compileInitialization(){
		temp = temp;
	}
	String generateInitialization(){
		temp = temp;
		temp = temp;
	}
	CompileError> compileMethod(){
		temp = temp;
	}
	String generateMethod(){
		temp = temp;
	}
	CompileError> compileStatement(){
		temp = temp;
		temp = temp;
	}
	CompileError> compileValue(){
		temp = temp;
		temp = temp;
	}
	String generateStatement(){
		temp = temp;
	}
	CompileError> truncateLeft(){
		temp = temp;
		temp = temp;
	}
	String generateDefinition(){
		temp = temp;
		temp = temp;
		temp = temp;
	}
	CompileError> compileDefinition(){
		temp = temp;
	}
	boolean isSymbol(){
		temp = temp;
		return true;
	}
	String generateDefinition(){
		temp = temp;
	}
	CompileError> truncateRight(){
		temp = temp;
		temp = temp;
	}
	CompileError> split(){
		return locator.locate(input).<Result<Tuple<String, String>, CompileError>>map(index -> {
            final var left = input.substring(0, index);
            final var right = input.substring(index + locator.length());
            final var tuple = new Tuple<>(left, right);
            return new Ok<>(tuple);
        }).orElseGet(() -> new Err<>(new CompileError("Infix '" + locator.unwrap() + "' not present", input)));
	}
	List<CompileError>>> prepare(){
		temp = temp;
	}
};