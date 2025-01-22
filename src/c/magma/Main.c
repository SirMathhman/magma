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

static void main(&[String] args){
	collect().mapErr(JavaError.new).mapErr(ApplicationError.new).mapValue(Main.runWithSources).match(Function.identity(), Optional.of).ifPresent(auto _lambda38_(auto error){
		return System.err.println(error.display());
	});
}

static Result<Set<Path>, IOException> collect(){
	return JavaFiles.walkWrapped(SOURCE_DIRECTORY).mapValue(auto _lambda39_(auto paths){
		return paths.stream().filter(Files.isRegularFile).filter(auto _lambda40_(auto path){
			return path.toString().endsWith(".java");
		}).collect(Collectors.toSet());
	});
}

static Optional<ApplicationError> runWithSources(Set<Path> sources){
	return sources.stream().map(Main.runWithSource).flatMap(Optional.stream).findFirst();
}

static Optional<ApplicationError> runWithSource(Path source){
	const auto relative=SOURCE_DIRECTORY.relativize(source);
	const auto parent=relative.getParent();
	const auto namespace=IntStream.range(0, parent.getNameCount()).mapToObj(parent.getName).map(Path.toString).toList();
	if(namespace.size() >= 2 && namespace.subList(0, 2).equals(List.of("magma", "java"))){
		return Optional.empty();
	}
	const auto nameWithExt=relative.getFileName().toString();
	const auto name=nameWithExt.substring(0, nameWithExt.indexOf('.''));
	const auto copy=new ArrayList<>(namespace);
	copy.add(name);
	System.out.println("Compiling source: "+String.join(".", copy));
	const auto targetParent=TARGET_DIRECTORY.resolve(parent);
	if(!Files.exists(targetParent)){
		const auto directoriesError=JavaFiles.createDirectoriesWrapped(targetParent);
		if(directoriesError.isPresent())return directoriesError.map(JavaError.new).map(ApplicationError.new);
	}
	return JavaFiles.readStringWrapped(source).mapErr(JavaError.new).mapErr(ApplicationError.new).flatMapValue(auto _lambda41_(auto input){
		return compile(input).mapErr(ApplicationError.new);
	}).mapValue(output -> writeOutput(output, targetParent, name)).match(Function.identity(), Optional.of);
}

static Result<String, CompileError> compile(String input){
	return JavaLang.createJavaRootRule().parse(input).flatMapValue(root1 -> Passer.pass(new State(), root1).mapValue(Tuple::right))
                .flatMapValue(auto _lambda42_(auto root){
		return CLang.createCRootRule().generate(root);
	});
}

static Optional<ApplicationError> writeOutput(String output, Path targetParent, String name){
	const auto target=targetParent.resolve(name+".c");
	const auto header=targetParent.resolve(name+".h");
	return JavaFiles.writeStringWrapped(target, output).or(() -> JavaFiles.writeStringWrapped(header, output))
                .map(JavaError.new).map(ApplicationError.new);
}
struct Main {static const Path SOURCE_DIRECTORY=Paths.get(".", "src", "java");static const Path TARGET_DIRECTORY=Paths.get(".", "src", "c");
}

