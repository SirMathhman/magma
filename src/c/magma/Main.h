import magma.api.Tuple;
import magma.api.result.Result;
import magma.app.Passer;
import magma.app.State;
import magma.app.error.ApplicationError;
import magma.app.error.CompileError;
import magma.app.error.JavaError;
import magma.java.JavaFiles;
import magma.app.lang.CLang;
import magma.app.lang.JavaLang;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
struct Main {
	static const Path SOURCE_DIRECTORY=Paths.get(".", "src", "java");
	static const Path TARGET_DIRECTORY=Paths.get(".", "src", "c");
	static void main(&[String] args){
		collect().mapErr(JavaError::new).mapErr(ApplicationError::new).mapValue(Main::runWithSources).match(Function.identity(), Optional::of).ifPresent(error ->System.err.println(error.display()));
	}
	static Result<Set<Path>, IOException> collect(){
		return JavaFiles.walkWrapped(SOURCE_DIRECTORY).mapValue(paths ->paths.stream().filter(Files::isRegularFile).filter(path ->path.toString().endsWith(".java")).collect(Collectors.toSet()));
	}
	static Optional<ApplicationError> runWithSources(Set<Path> sources){
		return sources.stream().map(Main::runWithSource).flatMap(Optional::stream).findFirst();
	}
	static Optional<ApplicationError> runWithSource(Path source){
		const var relative=SOURCE_DIRECTORY.relativize(source);
		const var parent=relative.getParent();
		const var namespace=IntStream.range(0, parent.getNameCount()).mapToObj(parent::getName).map(Path::toString).toList();
		if(namespace.size() >= 2 && namespace.subList(0, 2).equals(List.of("magma", "java"))){
			return Optional.empty();
		}
		const var nameWithExt=relative.getFileName().toString();
		const var name=nameWithExt.substring(0, nameWithExt.indexOf('.''));
		const var copy=new ArrayList<>(namespace);
		copy.add(name);
		System.out.println("Compiling source: "+String.join(".", copy));
		const var targetParent=TARGET_DIRECTORY.resolve(parent);
		if(!Files.exists(targetParent)){
			const var directoriesError=JavaFiles.createDirectoriesWrapped(targetParent);
			if(directoriesError.isPresent())return directoriesError.map(JavaError::new).map(ApplicationError::new);
		}
		return JavaFiles.readStringWrapped(source)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .flatMapValue(input ->compile(input).mapErr(ApplicationError::new))
                .mapValue(output -> writeOutput(output, targetParent, name)).match(Function.identity(), Optional::of);
	}
	static Result<String, CompileError> compile(String input){
		return JavaLang.createJavaRootRule().parse(input)
                .flatMapValue(root1 ->Passer.pass(new State(), root1).mapValue(Tuple::right))
                .flatMapValue(root ->CLang.createCRootRule().generate(root));
	}
	static Optional<ApplicationError> writeOutput(String output, Path targetParent, String name){
		const var target=targetParent.resolve(name+".c");
		const var header=targetParent.resolve(name+".h");
		return JavaFiles.writeStringWrapped(target, output)
                .or(() ->JavaFiles.writeStringWrapped(header, output))
                .map(JavaError::new).map(ApplicationError::new);
	}
}

