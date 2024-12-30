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
			auto __local5__={
				auto __local4__={
					auto __local3__={
						auto __local2__=JavaFiles.readString(source);
						__local2__.mapErr(__local2__, JavaError::new)
					};
					__local3__.mapErr(__local3__, ApplicationError::new)
				};
				__local4__.match(__local4__, input->runWithInput(source, input), Optional::of)
			};
			__local5__.ifPresent(__local5__, {
				auto __local1__=error->System.err;
				__local1__.println(__local1__, error.display())
			})
		}
	}
	Optional<ApplicationError> runWithInput(Path source, String input){
		return {
			auto __local21__={
				auto __local20__={
					auto __local19__={
						auto __local18__={
							auto __local17__={
								auto __local16__={
									auto __local15__={
										auto __local14__={
											auto __local13__=JavaLang.createJavaRootRule();
											__local13__.parse(__local13__, input)
										};
										__local14__.mapErr(__local14__, ApplicationError::new)
									};
									__local15__.flatMapValue(__local15__, parsed->writeAST(source.resolveSibling("Main.input.ast"), parsed))
								};
								__local16__.mapValue(__local16__, {
									auto __local12__={
										auto __local11__=node->new TreePassingStage(new Modifier());
										__local11__.pass(__local11__, new State(), node)
									};
									__local12__.right(__local12__)
								})
							};
							__local17__.mapValue(__local17__, {
								auto __local10__={
									auto __local9__=node->new TreePassingStage(new Formatter());
									__local9__.pass(__local9__, new State(), node)
								};
								__local10__.right(__local10__)
							})
						};
						__local18__.flatMapValue(__local18__, parsed->writeAST(source.resolveSibling("Main.output.ast"), parsed))
					};
					__local19__.flatMapValue(__local19__, {
						auto __local8__={
							auto __local7__={
								auto __local6__=parsed->CLang;
								__local6__.createCRootRule(__local6__)
							};
							__local7__.generate(__local7__, parsed)
						};
						__local8__.mapErr(__local8__, ApplicationError::new)
					})
				};
				__local20__.mapValue(__local20__, generated->writeGenerated(generated, source.resolveSibling("Main.c")))
			};
			__local21__.match(__local21__, value->value, Optional::of)
		};
	}
	Result<Node, ApplicationError> writeAST(Path path, Node node){
		return {
			auto __local25__={
				auto __local24__={
					auto __local23__={
						auto __local22__=JavaFiles.writeString(path, node.toString());
						__local22__.map(__local22__, JavaError::new)
					};
					__local23__.map(__local23__, ApplicationError::new)
				};
				__local24__.map(__local24__, Err::new)
			};
			__local25__.orElseGet(__local25__, ()->new Ok(node))
		};
	}
	Optional<ApplicationError> writeGenerated(String generated, Path target){
		return {
			auto __local27__={
				auto __local26__=JavaFiles.writeString(target, generated);
				__local26__.map(__local26__, JavaError::new)
			};
			__local27__.map(__local27__, ApplicationError::new)
		};
	}
}

