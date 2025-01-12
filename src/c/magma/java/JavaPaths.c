import java.nio.file.Paths;
struct JavaPaths {
	magma.io.Path get(String first, String... more){
		return JavaPath(Paths.get(first, more));
	}
}