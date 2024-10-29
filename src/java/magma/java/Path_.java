package magma.java;

import magma.option.Option;
import magma.result.Result;

import java.io.IOException;

public interface Path_ {
    JavaString computeFileNameWithoutExtension();

    Path_ resolveSibling(JavaString siblingName);

    Option<IOException> writeSafe(JavaString content);

    Result<JavaString, IOException> readString();

    boolean exists();
}
