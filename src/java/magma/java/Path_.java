package magma.java;

import magma.option.Option;
import magma.result.Result;

import java.io.IOException;

public interface Path_ {
    String computeFileNameWithoutExtension();

    Path_ resolveSibling(String siblingName);

    Option<IOException> writeSafe(String content);

    Result<String, IOException> readString();

    boolean exists();
}
