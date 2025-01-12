import magma.collect.Set;
import magma.option.Option;
import magma.result.Result;
import magma.stream.Stream;
struct Path {Stream<Path> streamNames();boolean exists();Result<Set<Path>, Error> walk();Result<String, Error> readString();Option<Error> writeString(String output);Option<Error> createDirectories();Path relativize(Path child);Option<Path> findParent();Path resolve(String segment);boolean isRegularFile();Path findFileName();
}