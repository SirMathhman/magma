#include "../magma/api/result/Result.h"
#include "../magma/app/pass/CFormatter.h"
#include "../magma/app/pass/InlinePassUnit.h"
#include "../magma/app/pass/PassUnit.h"
#include "../magma/app/pass/RootPasser.h"
#include "../magma/app/pass/TreePassingStage.h"
#include "../magma/app/error/ApplicationError.h"
#include "../magma/app/error/CompileError.h"
#include "../magma/app/error/JavaError.h"
#include "../magma/app/lang/CLang.h"
#include "../magma/app/lang/JavaLang.h"
#include "../magma/java/JavaFiles.h"
#include "../java/io/IOException.h"
#include "../java/nio/file/Files.h"
#include "../java/nio/file/Path.h"
#include "../java/nio/file/Paths.h"
#include "../java/util/ArrayList.h"
#include "../java/util/List.h"
#include "../java/util/Optional.h"
#include "../java/util/Set.h"
#include "../java/util/function/Function.h"
#include "../java/util/stream/Collectors.h"
#include "../java/util/stream/IntStream.h"
struct Main{
	Path SOURCE_DIRECTORY=Paths.get(".", "src", "java");
	Path TARGET_DIRECTORY=Paths.get(".", "src", "c");
	void main(String[] args){
		collect().mapErr(JavaError::new).mapErr(ApplicationError::new).mapValue(Main::runWithSources).match(Function.identity(), Optional::of).ifPresent(()->System.err.println(error.display()));
	}
	Result<Set<Path>, IOException> collect(){
		return JavaFiles.walkWrapped(SOURCE_DIRECTORY).mapValue(()->paths.stream().filter(Files::isRegularFile).filter(()->path.toString().endsWith(".java")).collect(Collectors.toSet()));
	}
	Optional<ApplicationError> runWithSources(Set<Path> sources){
		return sources.stream().map(Main::runWithSource).flatMap(Optional::stream).findFirst();
	}
	Optional<ApplicationError> runWithSource(Path source){
		var relative=SOURCE_DIRECTORY.relativize(source);
		var parent=relative.getParent();
		var namespace=IntStream.range(0, parent.getNameCount()).mapToObj(parent::getName).map(Path::toString).toList();
		if(namespace.size() >= 2 && namespace.subList(0, 2).equals(List.of("magma", "java"))){
			return Optional.empty();
		}
		var nameWithExt=relative.getFileName().toString();
		var name=nameWithExt.substring(0, nameWithExt.indexOf('.''));
		var copy=new ArrayList<>(namespace);
		copy.add(name);
		System.out.println("Compiling source: "+String.join(".", copy));
		var targetParent=TARGET_DIRECTORY.resolve(parent);
		if(!Files.exists(targetParent)){
			var directoriesError=JavaFiles.createDirectoriesWrapped(targetParent);
			if(directoriesError.isPresent())return directoriesError.map(JavaError::new).map(ApplicationError::new);
		}
		return JavaFiles.readStringWrapped(source).mapErr(JavaError::new).mapErr(ApplicationError::new).flatMapValue(()->compile(input, namespace).mapErr(ApplicationError::new)).mapValue(()->writeOutput(output, targetParent, name)).match(Function.identity(), Optional::of);
	}
	Result<String, CompileError> compile(String input, List<String> namespace){
		return JavaLang.createJavaRootRule().parse(input).flatMapValue(()->new TreePassingStage(new RootPasser()).pass(new InlinePassUnit<>(root, namespace)).mapValue(PassUnit::value)).flatMapValue(()->new TreePassingStage(new CFormatter()).pass(new InlinePassUnit<>(root, namespace)).mapValue(PassUnit::value)).flatMapValue(()->CLang.createCRootRule().generate(root));
	}
	Optional<ApplicationError> writeOutput(String output, Path targetParent, String name){
		var target=targetParent.resolve(name+".c");
		var header=targetParent.resolve(name+".h");
		return JavaFiles.writeStringWrapped(target, output).or(()->JavaFiles.writeStringWrapped(header, output)).map(JavaError::new).map(ApplicationError::new);
	}
}
