package magma.api.io;

import magma.api.option.Option;
import magma.api.result.Result;
import magma.api.stream.Stream;
import magma.java.JavaSet;

import java.io.IOException;

public interface Path {
    boolean isExists();

    Stream<Path> stream();

    Option<IOException> writeString(String output);

    Option<IOException> createAsDirectories();

    Result<String, IOException> readStrings();

    Result<JavaSet<Path>, IOException> walkWrapped();

    java.nio.file.Path unwrap();

    Path relativize(Path child);

    Option<Path> findParent();

    int getNameCount();

    Option<Path> getName(int index);

    String format();

    Path getFileName();

    Path resolvePath(Path child);

    Path resolveChild(String child);
}
