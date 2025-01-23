import magma.api.result.Result;import magma.app.Formatter;import magma.app.InlinePassUnit;import magma.app.PassUnit;import magma.app.RootPasser;import magma.app.TreePassingStage;import magma.app.error.ApplicationError;import magma.app.error.CompileError;import magma.app.error.JavaError;import magma.app.lang.CLang;import magma.app.lang.JavaLang;import magma.java.JavaFiles;import java.io.IOException;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.ArrayList;import java.util.List;import java.util.Optional;import java.util.Set;import java.util.stream.Collectors;import java.util.stream.IntStream;struct Main{
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
		return JavaFiles.readStringWrapped(source).mapErr(JavaError::new).mapErr(ApplicationError::new).flatMapValue(()->compile(input).mapErr(ApplicationError::new)).mapValue(()->writeOutput(output, targetParent, name)).match(Function.identity(), Optional::of);
	}
	Result<String, CompileError> compile(String input){
		return JavaLang.createJavaRootRule().parse(input).flatMapValue(()->new TreePassingStage(new RootPasser()).pass(new InlinePassUnit<>(root)).mapValue(PassUnit::value)).flatMapValue(()->new TreePassingStage(new Formatter()).pass(new InlinePassUnit<>(root)).mapValue(PassUnit::value)).flatMapValue(()->CLang.createCRootRule().generate(root));
	}
	Optional<ApplicationError> writeOutput(String output, Path targetParent, String name){
		var target=targetParent.resolve(name+".c");
		var header=targetParent.resolve(name+".h");
		return JavaFiles.writeStringWrapped(target, output).or(()->JavaFiles.writeStringWrapped(header, output)).map(JavaError::new).map(ApplicationError::new);
	}
}