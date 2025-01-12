package magma.io;

import magma.java.JavaSet;
import magma.option.Option;
import magma.result.Result;
import magma.stream.Stream;

public interface Path {
    Stream<Path> streamNames();

    boolean exists();

    Result<JavaSet<Path>, Error> walk();

    Result<String, Error> readString();

    Option<Error> writeString(String output);

    Option<Error> createDirectories();

    Path relativize(Path child);

    Option<Path> findParent();

    Path resolve(String segment);

    java.nio.file.Path unwrap();

    boolean isRegularFile();

    Path findFileName();
}
