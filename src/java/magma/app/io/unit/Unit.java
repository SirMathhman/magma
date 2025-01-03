package magma.app.io.unit;

import java.io.IOException;
import java.util.List;

public interface Unit {
    String computeName();

    List<String> computeNamespace();

    String read() throws IOException;
}
