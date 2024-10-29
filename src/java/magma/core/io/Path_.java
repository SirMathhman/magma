package magma.core.io;

import magma.core.String_;
import magma.core.option.Option;
import magma.core.result.Result;

import java.io.IOException;

public interface Path_ {
    String_ computeFileNameWithoutExtension();

    Path_ resolveSibling(String_ siblingName);

    Option<IOException> writeSafe(String_ content);

    Result<String_, IOException> readString();

    Option<IOException> deleteIfExists();

    boolean exists();
}
