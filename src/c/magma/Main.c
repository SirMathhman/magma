#include "../magma/api/io/Path.h"
#include "../magma/api/option/None.h"
#include "../magma/api/option/Option.h"
#include "../magma/api/option/Some.h"
#include "../magma/api/result/Result.h"
#include "../magma/api/stream/JoiningCollector.h"
#include "../magma/api/stream/Streams.h"
#include "../magma/app/error/ApplicationError.h"
#include "../magma/app/error/CompileError.h"
#include "../magma/app/error/JavaError.h"
#include "../magma/app/lang/CLang.h"
#include "../magma/app/lang/JavaLang.h"
#include "../magma/app/pass/CFormatter.h"
#include "../magma/app/pass/InlinePassUnit.h"
#include "../magma/app/pass/PassUnit.h"
#include "../magma/app/pass/RootPasser.h"
#include "../magma/app/pass/TreePassingStage.h"
#include "../magma/java/JavaFiles.h"
#include "../magma/java/JavaList.h"
#include "../magma/java/JavaListCollector.h"
#include "../magma/java/JavaPaths.h"
#include "../magma/java/JavaSet.h"
#include "../java/io/IOException.h"
struct Main{
	Path SOURCE_DIRECTORY=JavaPaths.get(".", "src", "java");
	Path TARGET_DIRECTORY=JavaPaths.get(".", "src", "c");
	void main(String[] args){
		collect().mapErr(JavaError::new).mapErr(ApplicationError::new).mapValue(Main::runWithSources).match(Function.identity(), Some::new).ifPresent(()->System.err.println(error.display()));
	}
	Result<JavaSet<Path>, IOException> collect(){
		return SOURCE_DIRECTORY.walkWrapped().mapValue(()->paths.stream().filter(JavaFiles::isRegularFile).filter(()->path.format().endsWith(".java")).collect(JavaSet.collect()));
	}
	Option<ApplicationError> runWithSources(JavaSet<Path> sources){
		return sources.stream().map(Main::runWithSource).flatMap(Streams::fromOption).next();
	}
	Option<ApplicationError> runWithSource(Path source){
		var relative=SOURCE_DIRECTORY.relativize(source);
		return relative.findParent().flatMap(()->runWithRelative(source, parent, relative));
	}
	Option<ApplicationError> runWithRelative(Path source, Path parent, Path relative){
		var namespace=parent.stream().map(Path::format).collect(new JavaListCollector<String>());
		if(shouldSkip(namespace)){
			return new None<>();
		}
		var nameWithExt=relative.getFileName().format();
		var name=nameWithExt.substring(0, nameWithExt.indexOf('.''));
		var copy=namespace.add(name);
		var joined=copy.stream().collect(new JoiningCollector(".")).orElse("");
		System.out.println("Compiling source: "+joined);
		var targetParent=TARGET_DIRECTORY.resolvePath(parent);
		if(!targetParent.isExists()){
			var directoriesError=targetParent.createAsDirectories();
			if(directoriesError.isPresent())return directoriesError.map(JavaError::new).map(ApplicationError::new);
		}
		return source.readStrings().mapErr(JavaError::new).mapErr(ApplicationError::new).flatMapValue(()->compile(input, namespace).mapErr(ApplicationError::new)).mapValue(()->writeOutput(output, targetParent, name)).match(Function.identity(), Some::new);
	}
	boolean shouldSkip(JavaList<String> namespace){
		return namespace.subList(0, 2).filter(()->slice.equals(JavaList.of("magma", "java"))).isPresent();
	}
	Result<String, CompileError> compile(String input, JavaList<String> namespace){
		return JavaLang.createJavaRootRule().parse(input).flatMapValue(()->new TreePassingStage(new RootPasser()).pass(new InlinePassUnit<>(root, namespace)).mapValue(PassUnit::value)).flatMapValue(()->new TreePassingStage(new CFormatter()).pass(new InlinePassUnit<>(root, namespace)).mapValue(PassUnit::value)).flatMapValue(()->CLang.createCRootRule().generate(root));
	}
	Option<ApplicationError> writeOutput(String output, Path targetParent, String name){
		var target=targetParent.resolveChild(name+".c");
		var header=targetParent.resolveChild(name+".h");
		return target.writeString(output).or(()->header.writeString(output)).map(JavaError::new).map(ApplicationError::new);
	}
}
