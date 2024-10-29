package magma.java;

import magma.option.Option;
import magma.result.Result;

import java.io.IOException;

public interface Path_ {
    String_ computeFileNameWithoutExtension();

    Path_ resolveSibling(String_ siblingName);

    Option<IOException> writeSafe(String_ content);

    Result<String_, IOException> readString();

    boolean exists();
}
