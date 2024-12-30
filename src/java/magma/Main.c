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
						__local2__.mapErr(__local2__, JavaError.new)
					};
					__local3__.mapErr(__local3__, ApplicationError.new)
				};
				__local4__.match(__local4__, input->runWithInput(source, input), Optional.of)
			};
			__local5__.ifPresent(__local5__, error->{
				auto __local1__=System.err;
				__local1__.println(__local1__, error.display())
			})
		}
	}
	Optional<ApplicationError> runWithInput(Path source, String input){
		return {
			auto __local20__={
				auto __local19__={
					auto __local18__={
						auto __local17__={
							auto __local16__={
								auto __local15__={
									auto __local14__={
										auto __local13__={
											auto __local12__=JavaLang.createJavaRootRule();
											__local12__.parse(__local12__, input)
										};
										__local13__.mapErr(__local13__, ApplicationError.new)
									};
									__local14__.flatMapValue(__local14__, parsed->writeAST(source.resolveSibling("Main.input.ast"), parsed))
								};
								__local15__.mapValue(__local15__, node->{
									auto __local11__={
										auto __local10__=new TreePassingStage(new Modifier());
										__local10__.pass(__local10__, new State(), node)
									};
									__local11__.right(__local11__)
								})
							};
							__local16__.mapValue(__local16__, node->{
								auto __local9__={
									auto __local8__=new TreePassingStage(new Formatter());
									__local8__.pass(__local8__, new State(), node)
								};
								__local9__.right(__local9__)
							})
						};
						__local17__.flatMapValue(__local17__, parsed->writeAST(source.resolveSibling("Main.output.ast"), parsed))
					};
					__local18__.flatMapValue(__local18__, parsed->{
						auto __local7__={
							auto __local6__=CLang.createCRootRule();
							__local6__.generate(__local6__, parsed)
						};
						__local7__.mapErr(__local7__, ApplicationError.new)
					})
				};
				__local19__.mapValue(__local19__, generated->writeGenerated(generated, source.resolveSibling("Main.c")))
			};
			__local20__.match(__local20__, value->value, Optional.of)
		};
	}
	Result<Node, ApplicationError> writeAST(Path path, Node node){
		return {
			auto __local24__={
				auto __local23__={
					auto __local22__={
						auto __local21__=JavaFiles.writeString(path, node.toString());
						__local21__.map(__local21__, JavaError.new)
					};
					__local22__.map(__local22__, ApplicationError.new)
				};
				__local23__.map(__local23__, Err.new)
			};
			__local24__.orElseGet(__local24__, ()->new Ok(node))
		};
	}
	Optional<ApplicationError> writeGenerated(String generated, Path target){
		return {
			auto __local26__={
				auto __local25__=JavaFiles.writeString(target, generated);
				__local25__.map(__local25__, JavaError.new)
			};
			__local26__.map(__local26__, ApplicationError.new)
		};
	}
}

