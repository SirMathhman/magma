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
		SOURCE_DIRECTORY  = temp;
		TARGET_DIRECTORY  = temp;
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
	CompileError> splitAndCompile(){
		temp = temp;
	}
	CompileError> split(){
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
	CompileError> compileStructSegment(){
		temp = temp;
	}
	CompileError> compileMethod(){
		temp = temp;
	}
	CompileError> compileStatement(){
		temp = temp;
	}
	CompileError> compileValue(){
		temp = temp;
	}
	String generateStatement(){
		temp = temp;
	}
	CompileError> truncateLeft(){
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