#include "magma/api/JavaFiles.h"
#include "magma/api/result/Err.h"
#include "magma/api/result/Ok.h"
#include "magma/api/result/Result.h"
#include "magma/compile/Node.h"
#include "magma/compile/State.h"
#include "magma/compile/error/ApplicationError.h"
#include "magma/compile/error/JavaError.h"
#include "magma/compile/lang/CLang.h"
#include "magma/compile/lang/JavaLang.h"
#include "magma/compile/pass/Flattener.h"
#include "magma/compile/pass/Modifier.h"
#include "magma/compile/pass/TreePassingStage.h"
#include "java/nio/file/Path.h"
#include "java/nio/file/Paths.h"
#include "java/util/Optional.h"
struct Main{
	void main(String[] args){
		final Path source=Paths.get(".", "src", "java", "magma", "Main.java");
		JavaFiles.readString(source).mapErr([JavaError](__lambda2__)->JavaError.new(__lambda2__)).mapErr([ApplicationError](__lambda1__)->ApplicationError.new(__lambda1__)).match([runWithInput, source](input)->runWithInput(source, input), [Optional](__lambda0__)->Optional.of(__lambda0__)).ifPresent([System](error)->System.err.println(error.display()));
	}
	Optional<ApplicationError> runWithInput(Path source, String input){
		return JavaLang.createJavaRootRule().parse(input).mapErr([ApplicationError](__lambda5__)->ApplicationError.new(__lambda5__)).flatMapValue([writeAST, source](parsed)->writeAST(source.resolveSibling("Main.input.ast"), parsed)).mapValue([TreePassingStage, State, Flattener](node)->new TreePassingStage(new Flattener()).pass(new State(), node).right()).mapValue([TreePassingStage, State, Modifier](node)->new TreePassingStage(new Modifier()).pass(new State(), node).right()).flatMapValue([writeAST, source](parsed)->writeAST(source.resolveSibling("Main.output.ast"), parsed)).flatMapValue([CLang, ApplicationError, __lambda4__](parsed)->CLang.createCRootRule().generate(parsed).mapErr([ApplicationError](__lambda4__)->ApplicationError.new(__lambda4__))).mapValue([writeGenerated, source](generated)->writeGenerated(generated, source.resolveSibling("Main.c"))).match([](value)->value, [Optional](__lambda3__)->Optional.of(__lambda3__));
	}
	Result<Node, ApplicationError> writeAST(Path path, Node node){
		return JavaFiles.writeString(path, node.toString()).map([JavaError](__lambda8__)->JavaError.new(__lambda8__)).map([ApplicationError](__lambda7__)->ApplicationError.new(__lambda7__)).<Result<Node, ApplicationError>>map([Err](__lambda6__)->Err.new(__lambda6__)).orElseGet([node, Ok]()->new Ok(node));
	}
	Optional<ApplicationError> writeGenerated(String generated, Path target){
		return JavaFiles.writeString(target, generated).map([JavaError](__lambda10__)->JavaError.new(__lambda10__)).map([ApplicationError](__lambda9__)->ApplicationError.new(__lambda9__));
	}
}

