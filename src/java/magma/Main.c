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
				__local6__.match(__local6__, {
					auto __function3__(auto input){
						return runWithInput(source, input);
					}
					__function3__
				}, Optional.of)
			};
			__local7__.ifPresent(__local7__, {
				auto __function1__(auto error){
					return {
						auto __local2__=System.err;
						__local2__.println(__local2__, error.display())
					};
				}
				__function1__
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
									__local23__.flatMapValue(__local23__, {
										auto __function20__(auto parsed){
											return writeAST(source.resolveSibling("Main.input.ast"), parsed);
										}
										__function20__
									})
								};
								__local24__.mapValue(__local24__, {
									auto __function17__(auto node){
										return {
											auto __local19__={
												auto __local18__=new TreePassingStage(new Modifier());
												__local18__.pass(__local18__, new State(), node)
											};
											__local19__.right(__local19__)
										};
									}
									__function17__
								})
							};
							__local25__.mapValue(__local25__, {
								auto __function14__(auto node){
									return {
										auto __local16__={
											auto __local15__=new TreePassingStage(new Formatter());
											__local15__.pass(__local15__, new State(), node)
										};
										__local16__.right(__local16__)
									};
								}
								__function14__
							})
						};
						__local26__.flatMapValue(__local26__, {
							auto __function13__(auto parsed){
								return writeAST(source.resolveSibling("Main.output.ast"), parsed);
							}
							__function13__
						})
					};
					__local27__.flatMapValue(__local27__, {
						auto __function10__(auto parsed){
							return {
								auto __local12__={
									auto __local11__=CLang.createCRootRule();
									__local11__.generate(__local11__, parsed)
								};
								__local12__.mapErr(__local12__, ApplicationError.new)
							};
						}
						__function10__
					})
				};
				__local28__.mapValue(__local28__, {
					auto __function9__(auto generated){
						return writeGenerated(generated, source.resolveSibling("Main.c"));
					}
					__function9__
				})
			};
			__local29__.match(__local29__, {
				auto __function8__(auto value){
					return value;
				}
				__function8__
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
			__local34__.orElseGet(__local34__, {
				auto __function30__(){
					return new Ok(node);
				}
				__function30__
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

