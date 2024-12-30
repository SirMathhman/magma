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
	void main(Array<String> args){
		final Path source=Paths.get(".", "src", "java", "magma", "Main.java");
		{
			auto __local7__={
				auto __local6__={
					auto __local5__={
						auto __local4__=JavaFiles.readString(source);
						__local4__.mapErr(__local4__, JavaError.new)
					};
					__local5__.mapErr(__local5__, ApplicationError.new)
				};
				__local6__.match(__local6__, auto __function3__(auto input){
					return runWithInput(source, input);
				}, Optional.of)
			};
			__local7__.ifPresent(__local7__, auto __function2__(auto error){
				return {
					auto __local1__=System.err;
					__local1__.println(__local1__, error.display())
				};
			})
		}
	}
	Optional<ApplicationError> runWithInput(Path source, String input){
		return {
			auto __local29__={
				auto __local28__={
					auto __local27__={
						auto __local26__={
							auto __local25__={
								auto __local24__={
									auto __local23__={
										auto __local22__={
											auto __local21__=JavaLang.createJavaRootRule();
											__local21__.parse(__local21__, input)
										};
										__local22__.mapErr(__local22__, ApplicationError.new)
									};
									__local23__.flatMapValue(__local23__, auto __function20__(auto parsed){
										return writeAST(source.resolveSibling("Main.input.ast"), parsed);
									})
								};
								__local24__.mapValue(__local24__, auto __function19__(auto node){
									return {
										auto __local18__={
											auto __local17__=new TreePassingStage(new Modifier());
											__local17__.pass(__local17__, new State(), node)
										};
										__local18__.right(__local18__)
									};
								})
							};
							__local25__.mapValue(__local25__, auto __function16__(auto node){
								return {
									auto __local15__={
										auto __local14__=new TreePassingStage(new Formatter());
										__local14__.pass(__local14__, new State(), node)
									};
									__local15__.right(__local15__)
								};
							})
						};
						__local26__.flatMapValue(__local26__, auto __function13__(auto parsed){
							return writeAST(source.resolveSibling("Main.output.ast"), parsed);
						})
					};
					__local27__.flatMapValue(__local27__, auto __function12__(auto parsed){
						return {
							auto __local11__={
								auto __local10__=CLang.createCRootRule();
								__local10__.generate(__local10__, parsed)
							};
							__local11__.mapErr(__local11__, ApplicationError.new)
						};
					})
				};
				__local28__.mapValue(__local28__, auto __function9__(auto generated){
					return writeGenerated(generated, source.resolveSibling("Main.c"));
				})
			};
			__local29__.match(__local29__, auto __function8__(auto value){
				return value;
			}, Optional.of)
		};
	}
	Result<Node, ApplicationError> writeAST(Path path, Node node){
		return {
			auto __local34__={
				auto __local33__={
					auto __local32__={
						auto __local31__=JavaFiles.writeString(path, node.toString());
						__local31__.map(__local31__, JavaError.new)
					};
					__local32__.map(__local32__, ApplicationError.new)
				};
				__local33__.map(__local33__, Err.new)
			};
			__local34__.orElseGet(__local34__, auto __function30__(){
				return new Ok(node);
			})
		};
	}
	Optional<ApplicationError> writeGenerated(String generated, Path target){
		return {
			auto __local36__={
				auto __local35__=JavaFiles.writeString(target, generated);
				__local35__.map(__local35__, JavaError.new)
			};
			__local36__.map(__local36__, ApplicationError.new)
		};
	}
}

