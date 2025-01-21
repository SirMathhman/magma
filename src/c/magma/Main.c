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
	 Path SOURCE_DIRECTORY=Paths.get(".", "src", "java");
	 Path TARGET_DIRECTORY=Paths.get(".", "src", "c");
	 void main(&[String] args){
		collect().mapErr(JavaError.new).mapErr(ApplicationError.new).mapValue(Main.runWithSources).match(Function.identity(), Optional.of).ifPresent(auto temp(){
			return System.err.println(error.display());
		});
	}
	 Result<Set<Path>, IOException> collect(){
		return auto temp(){
			return auto temp(){
				return path.toString().endsWith(".java"))
                .collect(Collectors.toSet()));
			};
		};
	}
	 Optional<ApplicationError> runWithSources(Set<Path> sources){
		return sources.stream().map(Main.runWithSource).flatMap(Optional.stream).findFirst();
	}
	 Optional<ApplicationError> runWithSource(Path source){
		 auto relative=SOURCE_DIRECTORY.relativize(source);
		 auto parent=relative.getParent();
		 auto namespace=IntStream.range(0, parent.getNameCount()).mapToObj(parent.getName).map(Path.toString).toList();
		if(namespace.size() >= 2 && namespace.subList(0, 2).equals(List.of("magma", "java"))){
			return Optional.empty();
		}
		 auto nameWithExt=relative.getFileName().toString();
		 auto name=nameWithExt.substring(0, nameWithExt.indexOf('.''));
		 auto copy=new ArrayList<>(namespace);
		copy.add(name);
		System.out.println("Compiling source: "+String.join(".", copy));
		 auto targetParent=TARGET_DIRECTORY.resolve(parent);
		if(!Files.exists(targetParent)){
			 auto directoriesError=JavaFiles.createDirectoriesWrapped(targetParent);
			if(directoriesError.isPresent())return directoriesError.map(JavaError.new).map(ApplicationError.new);
		}
		auto temp(){
			return compile(input).mapErr(ApplicationError::new))
                .mapValue(output -> writeOutput(output, targetParent, name)).match;
		}(Function.identity(), Optional.of);
	}
	 Result<String, CompileError> compile(String input){
		auto temp(){
			return Passer.pass(new State(), root1).mapValue(Tuple::right))
                .flatMapValue;
		}(auto temp(){
			return CLang.createCRootRule().generate(root);
		});
	}
	 Optional<ApplicationError> writeOutput(String output, Path targetParent, String name){
		 auto target=targetParent.resolve(name+".c");
		 auto header=targetParent.resolve(name+".h");
		auto temp(){
			return JavaFiles.writeStringWrapped(header, output))
                .map(JavaError.new).map;
		}(ApplicationError.new);
	}
}

