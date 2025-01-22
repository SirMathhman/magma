import magma.api.result.Result;import magma.app.InlinePassUnit;import magma.app.PassUnit;import magma.app.PassingStage;import magma.app.error.ApplicationError;import magma.app.error.CompileError;import magma.app.error.JavaError;import magma.app.lang.CLang;import magma.app.lang.JavaLang;import magma.java.JavaFiles;import java.io.IOException;import java.nio.file.Files;import java.nio.file.Path;import java.nio.file.Paths;import java.util.ArrayList;import java.util.List;import java.util.Optional;import java.util.Set;import java.util.stream.Collectors;import java.util.stream.IntStream;struct Main{
	Path SOURCE_DIRECTORY=Paths.get(".", "src", "java");
	Path TARGET_DIRECTORY=Paths.get(".", "src", "c");
	void main(String[] args);
	Result<Set<Path>, IOException> collect();
	Optional<ApplicationError> runWithSources(Set<Path> sources);
	Optional<ApplicationError> runWithSource(Path source);
	Result<String, CompileError> compile(String input);
	Optional<ApplicationError> writeOutput(String output, Path targetParent, String name);
}