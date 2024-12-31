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
#include "magma/compile/pass/Flattener0.h"
#include "magma/compile/pass/Generator.h"
#include "magma/compile/pass/Modifier.h"
#include "magma/compile/pass/TreePassingStage.h"
#include "java/nio/file/Path.h"
#include "java/nio/file/Paths.h"
#include "java/util/Optional.h"
struct Main{
	void main(String[] args){
		final Path source=Paths.get(".", "src", "java", "magma", "Main.java");
		JavaFiles.readString(source).mapErr(auto __function15__(auto __lambda2__){return JavaError.new(__lambda2__);}).mapErr(auto __function14__(auto __lambda1__){return ApplicationError.new(__lambda1__);}).match(auto __function12__(auto input){return runWithInput(source, input);}, auto __function13__(auto __lambda0__){return Optional.of(__lambda0__);}).ifPresent(auto __function11__(auto error){return System.err.println(error.display());});
	}
	Optional<ApplicationError> runWithInput(Path source, String input){
		final var generator=new Generator();
		return JavaLang.createJavaRootRule().parse(input).mapErr(auto __function26__(auto __lambda5__){return ApplicationError.new(__lambda5__);}).flatMapValue(auto __function25__(auto parsed){return writeAST(source.resolveSibling("Main.input.ast"), parsed);}).mapValue(auto __function24__(auto node){return new TreePassingStage(new Flattener(generator)).pass(new State(), node).right();}).mapValue(auto __function23__(auto node){return new TreePassingStage(new Modifier(generator)).pass(new State(), node).right();}).mapValue(auto __function22__(auto node){return new TreePassingStage(new Flattener0(generator)).pass(new State(), node).right();}).flatMapValue(auto __function21__(auto parsed){return writeAST(source.resolveSibling("Main.output.ast"), parsed);}).flatMapValue(auto __function19__(auto parsed){return CLang.createCRootRule().generate(parsed).mapErr(auto __function20__(auto __lambda4__){return ApplicationError.new(__lambda4__);});}).mapValue(auto __function18__(auto generated){return writeGenerated(generated, source.resolveSibling("Main.c"));}).match(auto __function16__(auto value){return value;}, auto __function17__(auto __lambda3__){return Optional.of(__lambda3__);});
	}
	Result<Node, ApplicationError> writeAST(Path path, Node node){
		return JavaFiles.writeString(path, node.toString()).map(auto __function30__(auto __lambda8__){return JavaError.new(__lambda8__);}).map(auto __function29__(auto __lambda7__){return ApplicationError.new(__lambda7__);}).<Result<Node, ApplicationError>>map(auto __function28__(auto __lambda6__){return Err.new(__lambda6__);}).orElseGet(auto __function27__(){return new Ok(node);});
	}
	Optional<ApplicationError> writeGenerated(String generated, Path target){
		return JavaFiles.writeString(target, generated).map(auto __function32__(auto __lambda10__){return JavaError.new(__lambda10__);}).map(auto __function31__(auto __lambda9__){return ApplicationError.new(__lambda9__);});
	}
}

