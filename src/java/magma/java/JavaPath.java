package magma.java;

import magma.option.Option;
import magma.result.Result;

import java.io.IOException;

public interface JavaPath {
    String computeFileNameWithoutExtension();

    JavaPath resolveSibling(String siblingName);

    Option<IOException> writeSafe(String content);

    Result<String, IOException> readString();

    boolean exists();
}
