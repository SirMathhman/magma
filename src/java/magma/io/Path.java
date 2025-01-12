package magma.io;

import magma.java.JavaSet;
import magma.option.Option;
import magma.result.Result;

public interface Path {
    boolean exists();

    Result<JavaSet<Path>, Error> walk();

    Result<String, Error> readString();

    Option<Error> writeString(String output);

    Option<Error> createDirectories();

    Path relativize(Path child);

    Option<Path> getParent();

    Path resolve(String segment);

    int getNameCount();

    Option<Path> getName(int index);

    java.nio.file.Path unwrap();

    boolean isRegularFile();

    Path getFileName();
}
