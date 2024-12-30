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
		{
			auto local={
				auto local={
					auto local={
						auto local=JavaFiles.readString(source);
						local.mapErr(local, JavaError::new)
					};
					local.mapErr(local, ApplicationError::new)
				};
				local.match(local, input->runWithInput(source, input), Optional::of)
			};
			local.ifPresent(local, {
				auto local=error->System.err;
				local.println(local, error.display())
			})
		}
	}
	Optional<ApplicationError> runWithInput(Path source, String input){
		return {
			auto local={
				auto local={
					auto local={
						auto local={
							auto local={
								auto local={
									auto local={
										auto local={
											auto local=JavaLang.createJavaRootRule();
											local.parse(local, input)
										};
										local.mapErr(local, ApplicationError::new)
									};
									local.flatMapValue(local, parsed->writeAST(source.resolveSibling("Main.input.ast"), parsed))
								};
								local.mapValue(local, {
									auto local={
										auto local=node->new TreePassingStage(new Modifier());
										local.pass(local, new State(), node)
									};
									local.right(local)
								})
							};
							local.mapValue(local, {
								auto local={
									auto local=node->new TreePassingStage(new Formatter());
									local.pass(local, new State(), node)
								};
								local.right(local)
							})
						};
						local.flatMapValue(local, parsed->writeAST(source.resolveSibling("Main.output.ast"), parsed))
					};
					local.flatMapValue(local, {
						auto local={
							auto local={
								auto local=parsed->CLang;
								local.createCRootRule(local)
							};
							local.generate(local, parsed)
						};
						local.mapErr(local, ApplicationError::new)
					})
				};
				local.mapValue(local, generated->writeGenerated(generated, source.resolveSibling("Main.c")))
			};
			local.match(local, value->value, Optional::of)
		};
	}
	Result<Node, ApplicationError> writeAST(Path path, Node node){
		return {
			auto local={
				auto local={
					auto local={
						auto local=JavaFiles.writeString(path, node.toString());
						local.map(local, JavaError::new)
					};
					local.map(local, ApplicationError::new)
				};
				local.map(local, Err::new)
			};
			local.orElseGet(local, ()->new Ok(node))
		};
	}
	Optional<ApplicationError> writeGenerated(String generated, Path target){
		return {
			auto local={
				auto local=JavaFiles.writeString(target, generated);
				local.map(local, JavaError::new)
			};
			local.map(local, ApplicationError::new)
		};
	}
}

