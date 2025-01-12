package magma.io;

import magma.java.JavaSet;
import magma.option.Option;
import magma.result.Result;

import java.io.IOException;

public interface Path {
    boolean exists();

    Result<JavaSet<Path>, IOException> walk();

    Result<String, IOException> readString();

    Option<IOException> writeString(String output);

    Option<IOException> createDirectories();

    Path relativize(Path child);

    Option<Path> getParent();

    Path resolve(String segment);

    int getNameCount();

    Option<Path> getName(int index);

    java.nio.file.Path unwrap();

    boolean isRegularFile();

    Path getFileName();
}
