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
#include "magma/compile/pass/Formatter.h"
#include "magma/compile/pass/Modifier.h"
#include "magma/compile/pass/TreePassingStage.h"
#include "java/nio/file/Path.h"
#include "java/nio/file/Paths.h"
#include "java/util/Optional.h"
struct Main{
	void main(String[] args){
		final Path source=Paths.get(".", "src", "java", "magma", "Main.java");
		JavaFiles.readString(source).mapErr(JavaError::new).mapErr(ApplicationError::new).match(input->runWithInput(source, input), Optional::of).ifPresent(error->System.err.println(error.display()));
	}
	Optional<ApplicationError> runWithInput(Path source, String input){
		return JavaLang.createJavaRootRule().parse(input).mapErr(ApplicationError::new).flatMapValue(parsed->writeAST(source.resolveSibling("Main.input.ast"), parsed)).mapValue(node->new TreePassingStage(new Modifier()).pass(new State(), node).right()).mapValue(node->new TreePassingStage(new Formatter()).pass(new State(), node).right()).flatMapValue(parsed->writeAST(source.resolveSibling("Main.output.ast"), parsed)).flatMapValue(parsed->CLang.createCRootRule().generate(parsed).mapErr(ApplicationError::new)).mapValue(generated->writeGenerated(generated, source.resolveSibling("Main.c"))).match(value->value, Optional::of);
	}
	Result<Node, ApplicationError> writeAST(Path path, Node node){
		return JavaFiles.writeString(path, node.toString()).map(JavaError::new).map(ApplicationError::new).<Result<Node, ApplicationError>>map(Err::new).orElseGet(()->new Ok(node));
	}
	Optional<ApplicationError> writeGenerated(String generated, Path target){
		return JavaFiles.writeString(target, generated).map(JavaError::new).map(ApplicationError::new);
	}
}

